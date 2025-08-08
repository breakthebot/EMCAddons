package org.breakthebot.EMCAddons.events;

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

import com.palmergames.bukkit.towny.object.TownyObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import org.breakthebot.EMCAddons.hideNSeek.HideNSeek;
import org.breakthebot.EMCAddons.hideNSeek.Listeners;
import org.breakthebot.EMCAddons.hideNSeek.utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainCMD implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /eventmanager <start|end|player|hunter|broadcast|giveall|status>").color(NamedTextColor.RED));
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> startEvent(player, args);
            case "end" -> endEvent(player);
            case "player" -> managePlayers(player, args);
            case "hunter" -> manageHunters(player, args);
            case "broadcast" -> broadcastEvent(player, args);
            case "giveall" -> giveAll(player, args);
            case "status" -> status(player);
            default -> player.sendMessage(Component.text("Usage: /eventmanager <start|end|player|hunter|broadcast|giveall|status>").color(NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (
                !sender.hasPermission("eventmanager.event.start") &&
                        !sender.hasPermission("eventmanager.event.end") &&
                        !sender.hasPermission("eventmanager.manage.players") &&
                        !sender.hasPermission("eventmanager.manage.hunters") &&
                        !sender.hasPermission("eventmanager.broadcast") &&
                        !sender.hasPermission("eventmanager.giveall")
        ) {
            return List.of();
        }

        if (args.length == 1) {
            return Stream.of("start", "end", "player", "hunter", "broadcast", "giveall", "status")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "start" -> TownyUniverse.getInstance().getTowns().stream()
                        .map(TownyObject::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                case "player" -> Stream.of("add", "remove", "list", "disqualify", "appeal")
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                case "hunter" -> Stream.of("add", "remove", "list")
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                case "giveall" -> Stream.of("player", "hunter")
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                default -> Collections.emptyList();
            };
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("hunter"))) {
            if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("disqualify") || args[1].equalsIgnoreCase("appeal")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    private void startEvent(Player player, String[] args) {
        if (!player.hasPermission("eventmanager.event.start")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /eventmanager start {town}").color(NamedTextColor.RED));
            return;
        }
        Town town = TownyAPI.getInstance().getTown(args[1]);
        if (town == null) {
            player.sendMessage(Component.text("Invalid town name. Usage: /eventmanager start {town}").color(NamedTextColor.RED));
            return;
        }

        HideNSeek current = EventManager.getInstance().getCurrent();
        if (current != null) {
            player.sendMessage(Component.text("There's already an active event for " + current.getHostTown().getName()).color(NamedTextColor.RED));
            return;
        }

        HideNSeek event = new HideNSeek(town);
        EventManager.getInstance().setCurrent(event);
        player.sendMessage(Component.text("Event started for town: " + town.getName()).color(NamedTextColor.GREEN));

        utils.broadcastGlobal("Hide & Seek event started for town: " + town.getName());
    }

    private void endEvent(Player player) {
        if (!player.hasPermission("eventmanager.event.end")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }
        if (EventManager.getInstance().getCurrent() == null) {
                player.sendMessage(Component.text("No active event to end.").color(NamedTextColor.RED));
                return;
            }
            EventManager.getInstance().endCurrent();
            player.sendMessage(Component.text("Event ended successfully").color(NamedTextColor.GREEN));
        }

    private void managePlayers(Player player, String[] args) {
        if (!player.hasPermission("eventmanager.manage.players")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /eventmanager player <add|remove|list|disqualify|appeal> [player]").color(NamedTextColor.RED));
            return;
        }

        String action = args[1].toLowerCase();
        HideNSeek event = EventManager.getInstance().getCurrent();
        if (event == null) {
            player.sendMessage(Component.text("No active event found. Start one with /eventmanager start {town}").color(NamedTextColor.RED));
            return;
        }

        List<Player> list = event.getPlayers();

        switch (action) {
            case "list" -> {
                if (list.isEmpty()) {
                    player.sendMessage(Component.text("There are currently no registered players").color(NamedTextColor.RED));
                    return;
                }
                String names = list.stream()
                        .map(Player::getName)
                        .collect(Collectors.joining(", "));

                Component msg = Component.text("There are currently " + list.size() + " registered players:\n")
                        .color(NamedTextColor.GREEN)
                        .append(Component.text(names)
                                .color(NamedTextColor.YELLOW));
                player.sendMessage(msg);
            }

            case "add", "remove", "disqualify", "appeal" -> {
                if (args.length < 3) {
                    player.sendMessage(Component.text("You must specify a player name for this action.").color(NamedTextColor.RED));
                    return;
                }

                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    OfflinePlayer offline = Bukkit.getOfflinePlayer(args[2]);
                    if (!(offline instanceof Player)) {
                        player.sendMessage(Component.text("Invalid player specified.").color(NamedTextColor.RED));
                        return;
                    }
                    target = (Player) offline;
                }

                switch (action) {
                    case "add" -> {
                        if (list.contains(target)) {
                            player.sendMessage(Component.text(target.getName() + " is already in the player list").color(NamedTextColor.RED));
                            return;
                        }
                        if (utils.isHunter(target)) {
                            player.sendMessage(Component.text(target.getName() + " is a hunter.").color(NamedTextColor.RED));
                            return;
                        }
                        if (utils.isDisqualified(target)) {
                            player.sendMessage(Component.text(target.getName() + " is a disqualified player.").color(NamedTextColor.RED));
                            return;
                        }
                        if (utils.hasItems(target)) {
                            player.sendMessage(Component.text(target.getName() + " must have an empty inventory to be added to the player list.").color(NamedTextColor.RED));
                            return;
                        }
                        list.add(target);
                        player.sendMessage(Component.text(target.getName() + " added to the player list").color(NamedTextColor.GREEN));
                        target.sendMessage(Component.text("You have been registered as a player for the Hide & Seek Event!").color(NamedTextColor.GREEN));
                    }
                    case "remove" -> {
                        if (!list.contains(target)) {
                            player.sendMessage(Component.text(target.getName() + " is not in the player list").color(NamedTextColor.RED));
                            return;
                        }
                        list.remove(target);
                        player.sendMessage(Component.text(target.getName() + " removed from the player list").color(NamedTextColor.GREEN));
                        target.sendMessage(Component.text("You have been removed as a player for the Hide & Seek Event!").color(NamedTextColor.RED));
                    }
                    case "disqualify" -> {
                        if (!list.contains(target)) {
                            player.sendMessage(Component.text(target.getName() + " is not in the player list").color(NamedTextColor.RED));
                            return;
                        }
                        Listeners.handleDisqualified(target);
                    }
                    case "appeal" -> {
                        if (!utils.isDisqualified(target)) {
                            player.sendMessage(Component.text(target.getName() + " is not disqualified.").color(NamedTextColor.RED));
                            return;
                        }
                        List<Player> disqualifed = event.getDisqualified();
                        disqualifed.remove(target);
                        event.setDisqualified(disqualifed);
                        player.sendMessage(Component.text(target.getName() + " is no longer disqualified.").color(NamedTextColor.GREEN));
                        target.sendMessage(Component.text("You are no longer disqualified from the Hide & Seek Event!").color(NamedTextColor.GREEN));
                    }
                }
            }
            default -> {
                player.sendMessage(Component.text("Usage: /eventmanager player <add|remove|list|disqualify|appeal> [player]").color(NamedTextColor.RED));
            }
        }
        event.setPlayers(list);
    }

    private void manageHunters(Player player, String[] args) {
        if (!player.hasPermission("eventmanager.manage.hunters")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /eventmanager hunter <add|remove|list> [hunter]").color(NamedTextColor.RED));
            return;
        }

        String action = args[1].toLowerCase();

        HideNSeek event = EventManager.getInstance().getCurrent();
        if (event == null) {
            player.sendMessage(Component.text("No active event found. Start one with /eventmanager start {town}").color(NamedTextColor.RED));
            return;
        }

        List<Player> list = event.getHunters();

        switch (action) {
            case "list" -> {
                if (list.isEmpty()) {
                    player.sendMessage(Component.text("There are currently no registered hunters").color(NamedTextColor.RED));
                    return;
                }
                String names = list.stream()
                        .map(Player::getName)
                        .collect(Collectors.joining(", "));

                Component msg = Component.text("There are currently " + list.size() + " registered hunters:\n")
                        .color(NamedTextColor.GREEN)
                        .append(Component.text(names)
                                .color(NamedTextColor.YELLOW));

                player.sendMessage(msg);
            }
            case "add", "remove" -> {
                if (args.length < 3) {
                    player.sendMessage(Component.text("You must specify a player name for this action.").color(NamedTextColor.RED));
                    return;
                }

                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    OfflinePlayer offline = Bukkit.getOfflinePlayer(args[2]);
                    if (!(offline instanceof Player)) {
                        player.sendMessage(Component.text("Invalid player specified.").color(NamedTextColor.RED));
                        return;
                    }
                    target = (Player) offline;
                }

                switch (action) {
                    case "add" -> {
                        if (list.contains(target)) {
                            player.sendMessage(Component.text(target.getName() + " is already in the hunters list").color(NamedTextColor.RED));
                            return;
                        }
                        if (utils.isPlayer(target)) {
                            player.sendMessage(Component.text(target.getName() + " is a player.").color(NamedTextColor.RED));
                            return;
                        }
                        list.add(target);
                        player.sendMessage(Component.text(target.getName() + " added to the hunters list").color(NamedTextColor.GREEN));
                        target.sendMessage(Component.text("You have been added as a hunter for the Hide & Seek Event!").color(NamedTextColor.GREEN));
                    }
                    case "remove" -> {
                        if (!list.contains(target)) {
                            player.sendMessage(Component.text(target.getName() + " is not in the hunters list").color(NamedTextColor.RED));
                            return;
                        }
                        list.remove(target);
                        player.sendMessage(Component.text(target.getName() + " removed from the hunters list").color(NamedTextColor.GREEN));
                        target.sendMessage(Component.text("You have been removed as a hunter for the Hide & Seek Event!").color(NamedTextColor.RED));
                    }
                }
            }
            default -> {
                player.sendMessage(Component.text("Usage: /eventmanager hunter <add|remove|list> [hunter]").color(NamedTextColor.RED));
            }
        }
        event.setHunters(list);
    }


    private void broadcastEvent(Player player, String[] args) {
        if (!player.hasPermission("eventmanager.broadcast")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }
        String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        utils.broadcastGlobal(msg);
    }

    public static void giveAll(Player player, String[] args) {
        if (!player.hasPermission("eventmanager.giveall")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }
        if (args.length != 2) {
            player.sendMessage(Component.text("Usage: /eventmanager giveall player|hunter").color(NamedTextColor.RED));
            return;
        }
        String action = args[1];
        List<Player> targets;
        if (action.equalsIgnoreCase("player")) {
            targets = utils.getPlayers();
        } else if (action.equalsIgnoreCase("hunter")) {
            targets = utils.getHunters();
        } else {
            player.sendMessage(Component.text("Invalid target audience chosen. <player|hunter>").color(NamedTextColor.RED));
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType().isAir()) {
            player.sendMessage(Component.text("You have no item in your main hand to duplicate.").color(NamedTextColor.RED));
            return;
        }

        for (Player target : targets) {
            ItemStack clonedItem = itemInHand.clone();
            target.getInventory().addItem(clonedItem);
            target.sendMessage(Component.text("You received loot from the event organisers!").color(NamedTextColor.GREEN));
        }
        player.sendMessage(Component.text("Gave item to " + targets.size() + " players.").color(NamedTextColor.GREEN));

        Component log = Component.text(player.getName() + " gave " + itemInHand.getType()
                        + " to " + targets.size() + " " + action.toLowerCase() + "s")
                .color(NamedTextColor.BLUE);
        utils.broadcastAdmins(log);
    }

    private void status(Player player) {
        if (!player.hasPermission("eventmanager.status")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }
        HideNSeek current = utils.getCurrentEvent();
        if (current == null) {
            player.sendMessage(Component.text("No ongoing event.").color(NamedTextColor.RED));
            return;
        }
        Component status = Component.text("Event Stats:\n")
                .append(Component.text(current.getPlayers().size() + " players.\n").color(NamedTextColor.GREEN))
                .append(Component.text(current.getHunters().size() + " hunters.\n").color(NamedTextColor.GOLD))
                .append(Component.text(current.getDisqualified().size() + " disqualified.").color(NamedTextColor.RED));
        player.sendMessage(status);
    }
}
