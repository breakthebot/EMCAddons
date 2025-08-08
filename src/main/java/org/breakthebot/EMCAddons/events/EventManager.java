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
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.breakthebot.EMCAddons.EMCAddons;
import org.breakthebot.EMCAddons.hideNSeek.HideNSeek;
import org.breakthebot.EMCAddons.hideNSeek.Listeners;
import org.breakthebot.EMCAddons.hideNSeek.utils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EventManager {
    public static List<String> currentEvents = new ArrayList<>();
    private HideNSeek current;

    private static EventManager instance;
    public static EventManager getInstance() {
        if (instance == null) { instance = new EventManager(); }
        return instance;
    }

    public void setCurrent(HideNSeek current) {
        currentEvents.add(current.getEventName());
        this.current = current;
    }

    public HideNSeek getCurrent() {
        return this.current;
    }

    public void endCurrent() {
        sendSummary(this.current);
        currentEvents.remove(this.current.getEventName());
        EMCAddons.getInstance().eventUnregister(this.current.getListenerInstance());
        Listeners.clearArrays();
        this.current = null;
    }

    private void sendSummary(HideNSeek instance) {
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

        utils.broadcastGlobal(summary);
        utils.broadcastPlayers(current, "Thank you for playing!");
        utils.broadcastHunters(current, "Thank you for seeking!");

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
