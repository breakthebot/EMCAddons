package org.breakthebot.EMCAddons.vanish;

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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


public class VanishManager {
    private static Plugin plugin;

    public static void init(Plugin pluginInstance) {
        plugin = pluginInstance;
    }

    private static boolean checkImmune(Player player) {
        return player.hasPermission("ly.skynet.see.specvanish");
    }

    private static boolean checkAllowed(Player player) {
        return player.hasPermission("ly.skynet.specvanish");
    }

    private static boolean checkVanished(Player player) {
        return checkAllowed(player) && player.getGameMode().equals(GameMode.SPECTATOR);
    }

    public static void vanish(Player staff) {
        if (!checkAllowed(staff)) {
            staff.sendMessage(Component.text("You do not have permission to go into vanish.").color(NamedTextColor.RED));
            return;
        }
        int total = 0;
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!checkImmune(other)) {
                other.hidePlayer(plugin, staff);
                total++;
            }
        }
        staff.sendMessage(Component.text("You are now vanished to " + total + " players.").color(NamedTextColor.GREEN));
    }

    public static void reveal(Player staff) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, staff);
        }
        staff.sendMessage(Component.text("You are now visible to players.").color(NamedTextColor.GREEN));
    }

    public static void handleJoin(Player player) {
        if (checkVanished(player)) {
            vanish(player);
        }
        if (checkImmune(player)) { return; }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            if (checkVanished(online)) {
                player.hidePlayer(plugin, online);
            }
        }
    }
}
