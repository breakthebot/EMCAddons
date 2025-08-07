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

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Town;
import org.breakthebot.EMCAddons.EMCAddons;
import org.breakthebot.EMCAddons.events.manager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class gameListeners implements Listener {

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Entity damager = event.getDamager();
        Player attacker;

        if (damager instanceof Player) {
            attacker = (Player) damager;
        } else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        } else { return; }

        hideNSeek current = manager.getInstance().getCurrent();
        if (current == null || current.getHunters().contains(attacker)) return;

        TownyAPI API = TownyAPI.getInstance();
        Town town1 = API.getTown(victim.getLocation());
        Town town2 = API.getTown(attacker.getLocation());
        if (town1 == null || !town1.equals(town2)) return;
        if (town1.equals(current.getHostTown())) {
            event.setCancelled(true);
            TownyMessaging.sendErrorMsg(attacker, "You may not attack this player as you are not a hunter in this Event.");
        }
    }
    private static final Set<UUID> pendingDisqualification = new HashSet<>();
    public static void clearPending() { pendingDisqualification.clear(); }

    @EventHandler
    public void onLogOff(PlayerQuitEvent event) {
        hideNSeek current = manager.getInstance().getCurrent();
        Player player = event.getPlayer();
        if (current == null || !current.getPlayers().contains(player)) return;
        UUID uuid = player.getUniqueId();
        pendingDisqualification.add(uuid);

        new BukkitRunnable() {
           @Override
           public void run() {
               if (pendingDisqualification.contains(uuid)) {
                   List<Player> list = current.getDisqualified();
                   list.add(player);
                   current.setDisqualified(list);
                   pendingDisqualification.remove(uuid);
               }
           }
        }.runTaskLater(EMCAddons.getInstance(), 20L * 60);
    }

    @EventHandler
    public void onLogIn(PlayerJoinEvent event) {
        pendingDisqualification.remove(event.getPlayer().getUniqueId());
    }
}
