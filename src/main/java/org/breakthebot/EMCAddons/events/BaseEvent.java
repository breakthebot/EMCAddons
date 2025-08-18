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

import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class BaseEvent {
    protected String eventName;
    protected Town hostTown;
    protected List<UUID> players = new ArrayList<>();
    protected boolean hasStarted;

    public BaseEvent(String name, Town town) {
        this.eventName = name;
        this.hostTown = town;
    }

    public void addPlayer(UUID uuid) { if (!isPlayer(uuid)) this.players.add(uuid); }
    public void addPlayer(Player player) { if (!isPlayer(player)) this.players.add(player.getUniqueId()); }
    public void removePlayer(UUID uuid) { this.players.remove(uuid); }
    public void removePlayer(Player player) { this.players.remove(player.getUniqueId()); }
    public boolean isPlayer(UUID uuid) { return this.getPlayerUUIDS().contains(uuid); }
    public boolean isPlayer(Player player) { return this.getPlayers().contains(player); }

    public List<UUID> getPlayerUUIDS() { return this.players; }
    public List<Player> getPlayers() {
        List<Player> objects = new ArrayList<>();
        for (UUID uuid : this.players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) objects.add(player);
        }
        return objects;
    }

    public boolean getStarted() { return this.hasStarted; }

    public String getEventName() { return eventName; }
    public Town getHostTown() { return this.hostTown; }

    public final void startEvent() {
        MainUtils.broadcastAdmins("Starting event: " + eventName);
        EventManager.addEvent(this);
        hasStarted = true;
        onStart();
    }

    public final void endEvent() {
        MainUtils.broadcastAdmins("Ending event: " + eventName);
        EventManager.removeEvent(this);
        hasStarted = false;
        onEnd();
    }

    protected abstract void onStart();
    protected abstract void onEnd();


    public void broadcastPlayers(String msg) {
        Component broadcastMessage = Component.text("[Player Broadcast] ")
                .color(NamedTextColor.BLUE)
                .append(Component.text(msg).color(NamedTextColor.GREEN));

        for (Player player : this.getPlayers()) {
            player.sendMessage(broadcastMessage);
        }
    }
}
