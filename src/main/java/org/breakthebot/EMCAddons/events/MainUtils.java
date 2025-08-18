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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MainUtils {


    public static void broadcastGlobal(String msg) {
        Component broadcastMessage = Component.text("[Event Broadcast] ")
                .color(NamedTextColor.BLUE)
                .append(Component.text(msg).color(NamedTextColor.GREEN));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(broadcastMessage);
        }
    }
    public static void broadcastGlobal(Component msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(msg);
        }
    }

    public static void broadcastAdmins(Component msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("eventmanager.admin")) continue;
            player.sendMessage(msg);
        }
    }

    public static void broadcastAdmins(String msg) {
        Component broadcastMessage = Component.text("[Admin Broadcast] ")
                .color(NamedTextColor.BLUE)
                .append(Component.text(msg).color(NamedTextColor.GREEN));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("eventmanager.admin")) continue;
            player.sendMessage(broadcastMessage);
        }
    }
}
