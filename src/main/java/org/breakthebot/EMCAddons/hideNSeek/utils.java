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
import org.breakthebot.EMCAddons.events.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class utils {

    public static @Nullable HideNSeek getCurrentEvent() {
        return EventManager.getInstance().getCurrent();
    }

    public static boolean isPlayer(Player player) {
        HideNSeek event = getCurrentEvent();
        if (event == null) return false;
        return event.getPlayers().contains(player);
    }

    public static boolean isHunter(Player player) {
        HideNSeek event = getCurrentEvent();
        if (event == null) return false;
        return event.getHunters().contains(player);
    }

    public static List<Player> getPlayers() {
        HideNSeek event = getCurrentEvent();
        if (event == null) return List.of();
        return event.getPlayers();
    }

    public static List<Player> getHunters() {
        HideNSeek event = getCurrentEvent();
        if (event == null) return List.of();
        return event.getHunters();
    }

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
