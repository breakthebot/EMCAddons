package org.breakthebot.EMCAddons.events.hideNSeek;

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
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.breakthebot.EMCAddons.events.MainUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class HideUtils {

    public static @Nullable HideNSeek getCurrentEvent() {
        return HideNSeek.getInstance();
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
        Component broadcastMessage = Component.text("[Player Broadcast] ")
                .color(NamedTextColor.BLUE)
                .append(Component.text(msg).color(NamedTextColor.GREEN));
        for (Player player : current.getPlayers()) {
            player.sendMessage(broadcastMessage);
        }
    }

    public static void broadcastHunters(HideNSeek current, String msg) {
        Component broadcastMessage = Component.text("[Hunter Broadcast] ")
                .color(NamedTextColor.BLUE)
                .append(Component.text(msg).color(NamedTextColor.GREEN));
        for (Player player : current.getHunters()) {
            player.sendMessage(broadcastMessage);
        }
    }

    public static boolean allowTabComplete(CommandSender sender) {
        return sender.hasPermission("eventmanager.hide-n-seek") ||
                sender.hasPermission("eventmanager.hide-n-seek.event.start") ||
                sender.hasPermission("eventmanager.hide-n-seek.event.end") ||
                sender.hasPermission("eventmanager.hide-n-seek.manage.player") ||
                sender.hasPermission("eventmanager.hide-n-seek.manage.hunter") ||
                sender.hasPermission("eventmanager.hide-n-seek.giveall") ||
                sender.hasPermission("eventmanager.hide-n-seek.status");
    }

    public static void sendSummary(HideNSeek instance) {
        List<Player> remainingPlayers = instance.getPlayers();
        List<Player> disqualifiedPlayers = instance.getDisqualified();
        List<Player> hunterPlayers = instance.getHunters();

        int remaining = remainingPlayers.size();
        int disqualified = disqualifiedPlayers.size();
        int participants = remaining + disqualified;
        int hunters = hunterPlayers.size();

        Component standingHover = getHover("Standing Players:\n", remainingPlayers);

        Component disqualifiedHover = getHover("Disqualified Players:\n", disqualifiedPlayers);

        Component huntersHover = getHover("Hunters:\n", hunterPlayers);

        Component summary = Component.text("[Event Broadcast] ")
                .color(NamedTextColor.BLUE)
                .append(Component.text("The Hide & Seek event has come to an end!\n")
                        .color(NamedTextColor.GREEN))
                .append(Component.text(remaining + " players stand!\n")
                        .color(NamedTextColor.GOLD)
                        .hoverEvent(HoverEvent.showText(standingHover))
                )
                .append(Component.text(disqualified + " players have been disqualified.\n")
                        .color(NamedTextColor.DARK_RED)
                        .hoverEvent(HoverEvent.showText(disqualifiedHover))
                )
                .append(Component.text(participants + " total participants, " + hunters + " total hunters.\n")
                        .color(NamedTextColor.GRAY)
                        .hoverEvent(HoverEvent.showText(huntersHover))
                )
                .append(Component.text("Thank you all for attending!")
                        .color(NamedTextColor.GOLD)
                );

        MainUtils.broadcastGlobal(summary);
        HideUtils.broadcastPlayers(instance, "Thank you for playing!");
        HideUtils.broadcastHunters(instance, "Thank you for seeking!");

    }
    private static @NotNull Component getHover(String content, List<Player> players) {
        Component standingHover = Component.text(content);

        if (players.isEmpty()) {
            standingHover = standingHover.append(Component.text("\n- None"));
        } else {
            for (Player player : players) {
                standingHover = standingHover
                        .append(Component.text("\n- " + player.getName()));
            }
        }

        return standingHover;
    }
}
