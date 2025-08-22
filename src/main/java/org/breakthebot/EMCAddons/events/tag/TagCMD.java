package org.breakthebot.EMCAddons.events.tag;

/*
 * This file is part of EMCAddons.
 *
 * EMCAddons is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EMCAddons is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EMCAddons. If not, see <https://www.gnu.org/licenses/>.
 */

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagCMD {

    public static boolean execute(@NotNull Player player, @NotNull String @NotNull [] args) {
        if (!TagUtils.allowTabComplete(player)) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return false;
        }
        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /em tag <start|end|player|hunter|giveall|status>").color(NamedTextColor.RED));
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "new" -> createEvent(player, args);
            case "start" -> startEvent(player);
            case "end" -> endEvent(player);
            case "player" -> managePlayers(player, args);
            case "status" -> status(player);
            default -> player.sendMessage(Component.text("Usage: /em tag <start|end|player|hunter|giveall|status>").color(NamedTextColor.RED));
        }
        return true;

    }

    public static List<String> TabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        List<String> perms = new ArrayList<>();
        if (sender.hasPermission("eventmanager.tag.event.create")) perms.add("new");
        if (sender.hasPermission("eventmanager.tag.event.start")) perms.add("start");
        if (sender.hasPermission("eventmanager.tag.event.end")) perms.add("end");
        if (sender.hasPermission("eventmanager.tag.manage.players")) perms.add("player");
        if (sender.hasPermission("eventmanager.tag.status")) perms.add("status");
        if (perms.isEmpty()) return List.of();

        if (args.length == 1) {
            return perms.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && perms.contains(args[0].toLowerCase())) {
            return switch (args[0].toLowerCase()) {
                case "new" -> TownyUniverse.getInstance().getTowns().stream()
                        .map(TownyObject::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                case "player" -> Stream.of("add", "remove", "list", "disqualify", "appeal")
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                default -> Collections.emptyList();
            };
        }

        if (args.length == 3 && perms.contains(args[0].toLowerCase())) {
            if (args[0].equalsIgnoreCase("player")) {
                if (args[1].equalsIgnoreCase("appeal")) {
                    Tag current = Tag.getInstance();
                    if (current == null) return List.of();
                    return current.getDisqualified().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                }
                if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")
                        || args[1].equalsIgnoreCase("disqualify")) {
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }


        return Collections.emptyList();
    }

    private static void createEvent(Player player, String[] args) {
        if (!player.hasPermission("eventmanager.tag.event.create")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /em tag new {town}").color(NamedTextColor.RED));
            return;
        }
        Town town = TownyAPI.getInstance().getTown(args[1]);
        if (town == null) {
            player.sendMessage(Component.text("Invalid town name. Usage: /em tag new {town}").color(NamedTextColor.RED));
            return;
        }

        Tag current = Tag.getInstance();
        if (current != null) {
            player.sendMessage(Component.text("There's already an active event at " + current.getHostTown().getName()).color(NamedTextColor.RED));
            return;
        }

        new Tag(town);
        player.sendMessage(Component.text("Event created for town: " + town.getName()).color(NamedTextColor.GREEN));
    }

    private static void startEvent(Player player) {
        if (!player.hasPermission("eventmanager.tag.event.start")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }
        Tag current = Tag.getInstance();
        if (current == null) {
            player.sendMessage(Component.text("No event to start found. Create one using /em tag new {town}").color(NamedTextColor.RED));
            return;
        }
        if (current.getStarted()) {
            player.sendMessage(Component.text("This event has already started!").color(NamedTextColor.RED));
            return;
        }
        if (current.getPlayers().size() < 2) {
            player.sendMessage(Component.text("You need at least 2 players to start this event!").color(NamedTextColor.RED));
            return;
        }
        if (current.getStarted()) {
            player.sendMessage(Component.text("This event was started already!").color(NamedTextColor.RED));
            return;
        }
        current.startEvent();
    }

    private static void endEvent(Player player) {
        if (!player.hasPermission("eventmanager.tag.event.end")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }
        Tag current = Tag.getInstance();
        if (current == null) {
            player.sendMessage(Component.text("No active event to end.").color(NamedTextColor.RED));
            return;
        }

        current.endEvent();
        player.sendMessage(Component.text("Event ended successfully").color(NamedTextColor.GREEN));
    }

    private static void managePlayers(Player player, String[] args) {
        if (!player.hasPermission("eventmanager.tag.manage.players")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /em tag player <add|remove|list|disqualify|appeal> [player]").color(NamedTextColor.RED));
            return;
        }

        String action = args[1].toLowerCase();
        Tag event = Tag.getInstance();
        if (event == null) {
            player.sendMessage(Component.text("No active event found. Start one with /em tag start {town}").color(NamedTextColor.RED));
            return;
        }

        List<Player> players = event.getPlayers();

        switch (action) {
            case "list" -> {
                if (players.isEmpty()) {
                    player.sendMessage(Component.text("There are currently no registered players").color(NamedTextColor.RED));
                    return;
                }
                String playerNames = players.stream()
                        .map(Player::getName)
                        .collect(Collectors.joining(", "));
                List<Player> tagged = event.getTagged();

                String taggedNames = tagged.stream()
                        .map(Player::getName)
                        .collect(Collectors.joining(", "));

                Component msg = Component.text("There are currently " + players.size() + " registered players:\n", NamedTextColor.GREEN)
                        .append(Component.text("Players: " + playerNames, NamedTextColor.YELLOW));

                if (!taggedNames.isEmpty()) {
                    msg = msg.append(Component.newline())
                            .append(Component.text("Tagged: " + taggedNames, NamedTextColor.RED));
                }

                player.sendMessage(msg);

            }

            case "add", "remove", "disqualify", "appeal" -> {
                if (args.length < 3) {
                    player.sendMessage(Component.text("You must specify a player name for this action.").color(NamedTextColor.RED));
                    return;
                }

                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    player.sendMessage(Component.text("Invalid player specified.").color(NamedTextColor.RED));
                    return;
                }

                switch (action) {
                    case "add" -> {
                        if (players.contains(target)) {
                            player.sendMessage(Component.text(target.getName() + " is already in the player list").color(NamedTextColor.RED));
                            return;
                        }
                        if (event.isTagged(target)) {
                            player.sendMessage(Component.text(target.getName() + " is tagged.").color(NamedTextColor.RED));
                            return;
                        }
                        if (event.isDisqualified(target)) {
                            player.sendMessage(Component.text(target.getName() + " is a disqualified player.").color(NamedTextColor.RED));
                            return;
                        }

                        event.addPlayer(target);
                        player.sendMessage(Component.text(target.getName() + " added to the player list").color(NamedTextColor.GREEN));
                        target.sendMessage(Component.text("You have been registered as a player for the Tag Event!").color(NamedTextColor.GREEN));
                    }
                    case "remove" -> {
                        if (!players.contains(target)) {
                            player.sendMessage(Component.text(target.getName() + " is not in the player list").color(NamedTextColor.RED));
                            return;
                        }
                        event.removePlayer(target);
                        player.sendMessage(Component.text(target.getName() + " removed from the player list").color(NamedTextColor.GREEN));
                        target.sendMessage(Component.text("You have been removed as a player for the Tag Event!").color(NamedTextColor.RED));
                    }
                    case "disqualify" -> {
                        if (!players.contains(target)) {
                            player.sendMessage(Component.text(target.getName() + " is not in the player list").color(NamedTextColor.RED));
                            return;
                        }
                        event.addDisqualified(target);
                        target.sendMessage(Component.text("You have been disqualified from the Tag Event!").color(NamedTextColor.GREEN));
                    }
                    case "appeal" -> {
                        if (!event.isDisqualified(target)) {
                            player.sendMessage(Component.text(target.getName() + " is not disqualified.").color(NamedTextColor.RED));
                            return;
                        }
                        event.removeDisqualified(target);
                        player.sendMessage(Component.text(target.getName() + " is no longer disqualified.").color(NamedTextColor.GREEN));
                        target.sendMessage(Component.text("You are no longer disqualified from the Tag Event!").color(NamedTextColor.GREEN));
                    }
                }
            }
            default -> player.sendMessage(Component.text("Usage: /em tag player <add|remove|list|disqualify|appeal> [player]").color(NamedTextColor.RED));
        }
    }

    private static void status(Player player) {
        if (!player.hasPermission("eventmanager.tag.status")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }
        Tag current = Tag.getInstance();
        if (current == null) {
            player.sendMessage(Component.text("No ongoing event.").color(NamedTextColor.RED));
            return;
        }
        Component status = Component.text("Event Stats:\n")
                .color(NamedTextColor.BLUE)
                .append(Component.text(current.getPlayers().size() + " players.\n").color(NamedTextColor.GREEN))
                .append(Component.text(current.getTagged().size() + " tagged players.\n").color(NamedTextColor.GOLD));
        player.sendMessage(status);
    }

}
