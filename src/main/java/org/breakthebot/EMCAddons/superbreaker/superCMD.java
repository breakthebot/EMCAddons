package org.breakthebot.EMCAddons.superbreaker;

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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class superCMD implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("emcaddons.superbreak")) {
            player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("toggle")) {
            superUtils.toggleSuperBreak(player);
            player.sendMessage(Component.text("SuperBreak " + (superUtils.hasSuperBreak(player) ? "enabled!" : "disabled!")).color(NamedTextColor.GREEN));
            return true;
        }
        String toggle = args[0];
        if (toggle.equalsIgnoreCase("on")) {
            if (superUtils.hasSuperBreak(player)) {
                player.sendMessage(Component.text("You already have superbreaker enabled.").color(NamedTextColor.RED));
                return true;
            }
            superUtils.giveSuperBreak(player.getUniqueId());
            player.sendMessage(Component.text("SuperBreak enabled.").color(NamedTextColor.GREEN));
        } else if (toggle.equalsIgnoreCase("off")) {
            if (!superUtils.hasSuperBreak(player)) {
                player.sendMessage(Component.text("You already have superbreaker disabled.").color(NamedTextColor.RED));
                return true;
            }
            superUtils.removeSuperBreak(player.getUniqueId());
            player.sendMessage(Component.text("SuperBreak disabled.").color(NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Usage: /superbreak on|off|toggle"));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("emcaddons.superbreak")) {
            return List.of();
        }
        if (args.length == 1) {
            return Stream.of("on", "off", "toggle")
                    .filter(opt -> opt.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
