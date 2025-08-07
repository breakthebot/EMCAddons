package org.breakthebot.EMCAddons.hideNSeek;

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

public class utils {

    public static void broadcastPlayers(HideNSeek current, String msg) {
        for (Player player : current.getPlayers()) {
            Component broadcastMessage = Component.text("[Event Broadcast] ")
                    .color(NamedTextColor.BLUE)
                    .append(Component.text(msg).color(NamedTextColor.GREEN));

            player.sendMessage(broadcastMessage);
        }
    }

    public static void broadcastHunters(HideNSeek current, String msg) {
        for (Player player : current.getHunters()) {
            Component broadcastMessage = Component.text("[Hunter Broadcast] ")
                    .color(NamedTextColor.BLUE)
                    .append(Component.text(msg).color(NamedTextColor.GREEN));

            player.sendMessage(broadcastMessage);
        }
    }

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
            player.sendMessage("[Event Boradcast] " + msg);
        }
    }

}
