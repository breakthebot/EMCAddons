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

import net.kyori.adventure.text.format.NamedTextColor;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import org.breakthebot.EMCAddons.hideNSeek.hideNSeek;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class command implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("")) { return false; }

        if (args.length == 0) {
            player.sendMessage(NamedTextColor.RED + "Usage: /eventmanager <start|end>");
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> startEvent(player, args);
            case "end" -> endEvent();
            case "player" -> managePlayers(player, args);
            case "hunter" -> manageHunters(player, args);
            default -> player.sendMessage(NamedTextColor.RED + "Usage: /eventmanager <start|end|player|hunter>");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "end", "player", "hunter").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "start":
                    return TownyUniverse.getInstance().getTowns().stream()
                            .map(town -> town.getName())
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "player":
                case "hunter":
                    return Arrays.asList("add", "remove").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "end":
                    return Collections.emptyList();
            }
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("hunter"))) {
            if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }


    private void startEvent(Player player, String[] args) {
        Town town = TownyAPI.getInstance().getTown(args[1]);
        if (town == null) {
            player.sendMessage(NamedTextColor.RED + "Invalid town name. Usage: /eventmanager start {town}");
            return;
        }
        new hideNSeek(town);
    }

    private void endEvent() {
        manager.getInstance().endCurrent();
    }

    private void managePlayers(Player player, String[] args) {
        String action = args[1].toLowerCase();
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(args[2]);
            if ((!(offline instanceof Player))) {
                player.sendMessage(NamedTextColor.RED + "Invalid player specified.");
                return;
            }
            target = (Player) offline;
        }
        hideNSeek event = manager.getInstance().getCurrent();
        List<Player> list = event.getPlayers();

        if (action.equals("add")) {
            list.add(target);
        } else if (action.equals("remove")) {
            list.remove(target);
        } else {
            player.sendMessage(NamedTextColor.RED + "Usage: /eventmanager player add|remove {player}");
            return;
        }
        event.setPlayers(list);
    }

    private void manageHunters(Player player, String[] args) {
        String action = args[1].toLowerCase();
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(args[2]);
            if ((!(offline instanceof Player))) {
                player.sendMessage(NamedTextColor.RED + "Invalid player specified.");
                return;
            }
            target = (Player) offline;
        }
        hideNSeek event = manager.getInstance().getCurrent();
        List<Player> list = event.getHunters();

        if (action.equals("add")) {
            list.add(target);
        } else if (action.equals("remove")) {
            list.remove(target);
        } else {
            player.sendMessage(NamedTextColor.RED + "Usage: /eventmanager hunter add|remove {hunter}");
            return;
        }
        event.setHunters(list);
    }
}
