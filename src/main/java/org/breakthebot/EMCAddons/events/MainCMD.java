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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.breakthebot.EMCAddons.events.hideNSeek.*;
import org.breakthebot.EMCAddons.events.tag.TagCMD;
import org.breakthebot.EMCAddons.events.tag.TagUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class MainCMD implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /em {event} {action} {args}").color(NamedTextColor.RED));
            return false;
        }
        String[] argsExcludingMain = Arrays.copyOfRange(args, 1, args.length);

        switch (args[0].toLowerCase()) {
            case "broadcast" -> broadcastEvent(player, args);
            case "list" -> list(player);
            case "hide-n-seek" -> HideCMD.execute(player, argsExcludingMain);
            case "tag" -> TagCMD.execute(player, argsExcludingMain);
            default -> player.sendMessage(Component.text("Usage: /em {event} {action} {args}").color(NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> eventComplete = new ArrayList<>();
        if (HideUtils.allowTabComplete(sender)) eventComplete.add("hide-n-seek");
        if (TagUtils.allowTabComplete(sender)) eventComplete.add("tag");
        if (sender.hasPermission("eventmanager.list")) eventComplete.add("list");
        if (sender.hasPermission("eventmanager.broadcast")) eventComplete.add("broadcast");
        if (eventComplete.isEmpty()) return List.of();

        if (args.length == 1) {
            return eventComplete.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        String[] argsExcludingMain = Arrays.copyOfRange(args, 1, args.length);

        if (args.length > 1) {
            switch (args[0].toLowerCase()) {
                case "hide-n-seek" -> {
                    return HideCMD.TabComplete(sender, argsExcludingMain);
                }
                case "tag" -> {
                    return TagCMD.TabComplete(sender, argsExcludingMain);
                }
                default -> {
                    return Collections.emptyList();
                }
            }
        }

        return Collections.emptyList();
    }

    private static void broadcastEvent(Player player, String[] args) {
        if (!player.hasPermission("eventmanager.broadcast")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }
        String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        MainUtils.broadcastGlobal(msg);
    }

    private static void list(Player player) {
        if (!player.hasPermission("eventmanager.list")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return;
        }
        if (EventManager.getCurrentEventNames().isEmpty()) {
            player.sendMessage(Component.text("No active events.").color(NamedTextColor.RED));
            return;
        }
        Component msg = Component.text("Active events: \n")
                .color(NamedTextColor.BLUE)
                .append(Component.text(String.join(", ", EventManager.getCurrentEventNames())).color(NamedTextColor.GREEN));
        player.sendMessage(msg);
    }
}
