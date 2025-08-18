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

public class HideNSeek extends BaseEvent {
    private final Town hostTown;
    private List<Player> players;
    private List<Player> hunters;
    private List<Player> disqualified;
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
        EMCAddons.getInstance().eventRegister(this.listenerInstance);
    }

    @Override
    protected void onEnd() {
        HideUtils.sendSummary(this);
        EMCAddons.getInstance().eventUnregister(this.getListenerInstance());
        HideListeners.clearArrays();
        instance = null;
    }

    public List<Player> getPlayers() { return this.players; }
    public void setPlayers(List<Player> players) { this.players = players; }
    public List<Player> getHunters() { return this.hunters; }
    public void setHunters(List<Player> hunters) { this.hunters = hunters; }
    public List<Player> getDisqualified() { return disqualified; }
    public void setDisqualified(List<Player> disqualified) { this.disqualified = disqualified; }

}
