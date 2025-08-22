package org.breakthebot.EMCAddons.superbreaker;

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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent ;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class superUtils implements Listener {

    private static final Set<UUID> superbreakers = new HashSet<>();

    public static boolean hasSuperBreak(Player player) {
        UUID uuid = player.getUniqueId();
        if (!superbreakers.contains(uuid)) return false;
        if (!player.hasPermission("emcaddons.superbreak")) {
            removeSuperBreak(player.getUniqueId());
            return false;
        }
        return true;
    }

    public static void giveSuperBreak(UUID uuid) { superbreakers.add(uuid); }

    public static void removeSuperBreak(UUID uuid) { superbreakers.remove(uuid); }

    public static void toggleSuperBreak(Player player) {
        if (hasSuperBreak(player)) removeSuperBreak(player.getUniqueId());
        else giveSuperBreak(player.getUniqueId());
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (!hasSuperBreak(player)) return;
        event.setInstaBreak(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeSuperBreak(event.getPlayer().getUniqueId());
    }
}
