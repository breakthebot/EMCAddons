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

import com.palmergames.bukkit.towny.object.Town;
import org.breakthebot.EMCAddons.EMCAddons;
import org.breakthebot.EMCAddons.events.BaseEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HideNSeek extends BaseEvent {
    private final Town hostTown;
    private final List<UUID> hunters;
    private final List<UUID> disqualified;
    public Listener listenerInstance;
    private static HideNSeek instance;


    public HideNSeek(Town town) {
        super("hide-n-seek", town);
        this.hostTown = town;
        this.players = new ArrayList<>();
        this.hunters = new ArrayList<>();
        this.disqualified = new ArrayList<>();
        instance = this;
    }

    public static HideNSeek getInstance() { return instance; }

    public Listener getListenerInstance() { return this.listenerInstance; }
    public Town getHostTown() { return this.hostTown; }

    @Override
    protected void onStart() {
        this.listenerInstance = new HideListeners();
        EMCAddons.getInstance().addListener(this.listenerInstance);
    }

    @Override
    protected void onEnd() {
        HideUtils.sendSummary(this);
        EMCAddons.getInstance().removeListener(this.getListenerInstance());
        instance = null;

        HideListeners.clearArrays();
        players.clear();
        hunters.clear();
        disqualified.clear();
    }

    public List<Player> getHunters(){
        return getPlayersFromUUID(this.hunters);
    }
    public void addHunter(UUID uuid) { if (!isHunter(uuid)) this.hunters.add(uuid); }
    public void addHunter(Player player) { if (!isHunter(player.getUniqueId())) this.hunters.add(player.getUniqueId()); }
    public void removeHunter(UUID uuid) { this.hunters.remove(uuid); }
    public void removeHunter(Player player) { this.hunters.remove(player.getUniqueId()); }
    public boolean isHunter(UUID uuid) { return this.hunters.contains(uuid); }
    public boolean isHunter(Player player) { return this.hunters.contains(player.getUniqueId()); }

    public List<Player> getDisqualified() {
        return getPlayersFromUUID(this.disqualified);
    }
    public void addDisqualified(UUID uuid) { if (!isDisqualified(uuid)) this.disqualified.add(uuid); }
    public void addDisqualified(Player player) { if (!isDisqualified(player.getUniqueId())) this.disqualified.add(player.getUniqueId()); }
    public void removeDisqualified(UUID uuid) { this.disqualified.remove(uuid); }
    public void removeDisqualified(Player player) { this.disqualified.remove(player.getUniqueId()); }
    public boolean isDisqualified(UUID uuid) { return this.disqualified.contains(uuid); }
    public boolean isDisqualified(Player player) { return this.disqualified.contains(player.getUniqueId()); }


    public void broadcastHunters(String msg) { broadcastAudience("[Hunter Broadcast] ", this.getHunters(), msg); }
}
