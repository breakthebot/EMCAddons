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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
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

    public static boolean isDisqualified(Player player) {
        HideNSeek event = getCurrentEvent();
        if (event == null) return false;
        return event.getDisqualified().contains(player);
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

    public static boolean hasItems(Player player) {
        PlayerInventory inv = player.getInventory();

        boolean hasInventoryItems = Arrays.stream(inv.getContents())
                .anyMatch(item -> item != null && item.getType() != Material.AIR);
        boolean hasArmorItems = Arrays.stream(inv.getArmorContents())
                .anyMatch(item -> item != null && item.getType() != Material.AIR);
        boolean hasOffhandItem = inv.getItemInOffHand().getType() != Material.AIR;
        boolean hasCursorItem = player.getOpenInventory().getCursor().getType() != Material.AIR;

        return hasInventoryItems || hasArmorItems || hasOffhandItem || hasCursorItem;
    }


    public static void broadcastPlayers(HideNSeek current, String msg) {
        for (Player player : current.getPlayers()) {
            Component broadcastMessage = Component.text("[Player Broadcast] ")
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
            player.sendMessage(msg);
        }
    }

    public static void broadcastAdmins(Component msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("eventmanager.admin")) continue;
            player.sendMessage(msg);
        }
    }


}
