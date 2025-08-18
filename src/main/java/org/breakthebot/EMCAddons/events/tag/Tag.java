package org.breakthebot.EMCAddons.events.tag;

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
import org.breakthebot.EMCAddons.EMCAddons;
import org.breakthebot.EMCAddons.events.BaseEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Tag extends BaseEvent {
    private final List<UUID> tagged;
    private final List<UUID> disqualified;
    private Listener listenerInstance;
    private static Tag instance;

    public Tag(Town town) {
        super("Tag", town);
        this.tagged = new ArrayList<>();
        this.disqualified = new ArrayList<>();
        instance = this;
    }

    public static @Nullable Tag getInstance() { return instance; }

    public List<Player> getTagged(){
        return getPlayersFromUUID(this.tagged);
    }
    public void addTagged(UUID uuid) { if (!isTagged(uuid)) this.tagged.add(uuid); }
    public void addTagged(Player player) { if (!isTagged(player.getUniqueId())) this.tagged.add(player.getUniqueId()); }
    public void removeTagged(UUID uuid) { this.tagged.remove(uuid); }
    public void removeTagged(Player player) { this.tagged.remove(player.getUniqueId()); }
    public boolean isTagged(UUID uuid) { return this.tagged.contains(uuid); }
    public boolean isTagged(Player player) { return this.tagged.contains(player.getUniqueId()); }

    public List<Player> getDisqualified() {
        return getPlayersFromUUID(this.disqualified);
    }
    public void addDisqualified(UUID uuid) { if (!isDisqualified(uuid)) this.disqualified.add(uuid); }
    public void addDisqualified(Player player) { if (!isDisqualified(player.getUniqueId())) this.disqualified.add(player.getUniqueId()); }
    public void removeDisqualified(UUID uuid) { this.disqualified.remove(uuid); }
    public void removeDisqualified(Player player) { this.disqualified.remove(player.getUniqueId()); }
    public boolean isDisqualified(UUID uuid) { return this.disqualified.contains(uuid); }
    public boolean isDisqualified(Player player) { return this.disqualified.contains(player.getUniqueId()); }


    @Override
    protected void onStart() {
        this.broadcastPlayers("Event has now started!");
        this.listenerInstance = new TagListeners();
        EMCAddons.getInstance().eventRegister(this.listenerInstance);

        int totalPlayers = this.players.size();
        int numToTag = (int) Math.ceil(totalPlayers * 0.05);
        numToTag = Math.max(numToTag, 1);

        List<Player> shuffled = new ArrayList<>(this.getPlayers());
        Collections.shuffle(shuffled);

        for (int i = 0; i < numToTag; i++) {
            Player player = shuffled.get(i);
            this.addTagged(player);
            TagUtils.setGlow(player);
            player.sendMessage(Component.text("You're IT. Tag others to lose it.").color(NamedTextColor.RED));
        }
    }

    @Override
    protected void onEnd() {
        TagUtils.sendSummary(instance);
        EMCAddons.getInstance().eventUnregister(this.listenerInstance);
        for (Player player : getTagged()) {
            TagUtils.removeGlow(player);
        }
        instance = null;

        TagListeners.clearArrays();
        players.clear();
        tagged.clear();
        disqualified.clear();
    }
}
