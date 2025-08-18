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

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PlayerChangePlotEvent;
import com.palmergames.bukkit.towny.event.SpawnEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.breakthebot.EMCAddons.events.MainUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TagListeners implements Listener {

    private static final Map<UUID, Long> recentTags = new HashMap<>();
    private static final Set<UUID> disqualifyOnLogin = new HashSet<>();
    public static final Map<UUID, ScheduledTask> disqualifyTasks = new ConcurrentHashMap<>();
    public static void clearArrays() {
        recentTags.clear();
        disqualifyOnLogin.clear();
        disqualifyTasks.clear();
    }


    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Tag current = Tag.getInstance();
        if (current == null) return;

        Entity damager = event.getDamager();
        Player attacker;

        if (damager instanceof Player) {
            attacker = (Player) damager;
        } else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        } else { return; }

        TownyAPI API = TownyAPI.getInstance();
        Town town = API.getTown(victim.getLocation());
        Town town2 = API.getTown(attacker.getLocation());
        if (town == null || !town.equals(town2)) return;
        if (!town.equals(current.getHostTown())) return;

        long now = System.currentTimeMillis();
        UUID victimID = victim.getUniqueId();
        UUID attackerID = attacker.getUniqueId();
        if (!current.isTagged(attacker)) {
            attacker.sendMessage(Component.text("You can't attack others as you're not IT!").color(NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }
        if (!current.isPlayer(victim)) {
            attacker.sendMessage(Component.text("You can't attack this player as they're not participating in the Tag Event!").color(NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }
        if (recentTags.containsKey(victimID) && recentTags.get(victimID) > now - 15_000L) {
            attacker.sendMessage(Component.text("This player was tagged recently! Find someone else.").color(NamedTextColor.RED));
            event.setCancelled(true);
            return;
        } else { recentTags.remove(victimID); }


        event.setDamage(0.0);
        victim.sendMessage(Component.text("You're it! You have been tagged.")
                .color(NamedTextColor.RED));
        attacker.sendMessage(Component.text("You have tagged " + victim.getName() + ", run!")
                .color(NamedTextColor.GREEN));

        Location location = victim.getLocation();
        location.getWorld().strikeLightningEffect(location);

        current.broadcastPlayers(victim.getName() + " has been tagged by " + attacker.getName() + "!");

        TagUtils.removeGlow(attacker);
        TagUtils.setGlow(victim);

        recentTags.put(attackerID, now);

        current.addTagged(victim);
        current.removeTagged(attacker);

        ScheduledTask task = disqualifyTasks.remove(attackerID);
        if (task != null) {
            task.cancel();
        }

        ScheduledTask newTask = TagUtils.scheduleDisqualify(victimID, 120);
        disqualifyTasks.put(victimID, newTask);
    }

    public static void disqualifyPlayer(UUID uuid) {
        Tag current = Tag.getInstance();
        if (current == null) return;
        if (!current.isPlayer(uuid)) return;

        current.removePlayer(uuid);
        current.addDisqualified(uuid);

        if (current.isTagged(uuid)) {
            current.removeTagged(uuid);
        }
        TagUtils.handleTags();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            disqualifyOnLogin.add(uuid);
            return;
        }

        if (player.isGlowing()) player.setGlowing(false);
        disqualifyOnLogin.remove(uuid);
        current.broadcastPlayers(player.getName() + " has been disqualified!");

        player.sendMessage(Component.text("You have been disqualified from the Tag event. If you believe this is a mistake, contact event organizers.")
                .color(NamedTextColor.RED));

        try {
            player.teleport(current.getHostTown().getSpawn());
        } catch (TownyException e) {
            MainUtils.broadcastAdmins("Could not teleport " + player.getName() + " to town spawn. Exception: \n" + e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Tag current = Tag.getInstance();
        if (current == null) { return; }

        UUID uuid = event.getPlayer().getUniqueId();
        if (disqualifyOnLogin.contains(uuid)) {
            disqualifyPlayer(uuid);
        }
    }

    @EventHandler
    public void onLogOff(PlayerQuitEvent event) {
        Tag current = Tag.getInstance();
        if (current == null) return;

        UUID uuid = event.getPlayer().getUniqueId();
        if (current.isPlayer(uuid) || current.isTagged(uuid)) {
            disqualifyPlayer(uuid);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Tag current = Tag.getInstance();
        if (current == null) return;

        Player player = event.getEntity();
        if (current.isPlayer(player) && !current.isDisqualified(player))  {
            disqualifyPlayer(player.getUniqueId());
        }
    }

    @EventHandler
    public void onTeleport(SpawnEvent event) {
        Tag current = Tag.getInstance();
        if (current == null) return;
        if (!current.isPlayer(event.getPlayer())) return;
        Town town = TownyAPI.getInstance().getTown(event.getFrom());
        if (town == null) return;
        if (current.getHostTown().equals(town)) {
            event.setCancelled(true);
            event.setCancelMessage("You are not allowed to teleport away while participating in the Tag event!");
        }
    }

    @EventHandler
    public void onPlotChange(PlayerChangePlotEvent event) {
        Tag current = Tag.getInstance();
        if (current == null) return;
        Player player = event.getPlayer();
        if (!current.isPlayer(player) || current.isDisqualified(player)) return;
        TownBlock origin = event.getFrom().getTownBlockOrNull();
        if (origin == null) return;
        Town town = origin.getTownOrNull();
        if (town == null) return;

        TownBlock targetBlock = event.getTo().getTownBlockOrNull();
        Town targetTown = null;
        if (targetBlock != null) { targetTown = targetBlock.getTownOrNull(); }

        if (town.equals(targetTown)) return;
        if (current.getHostTown().equals(town)) {
            disqualifyPlayer(player.getUniqueId());
        }
    }

    @EventHandler
    public void onECOpen(InventoryOpenEvent event) {
        Tag current = Tag.getInstance();
        if (current == null) return;

        if (!(event.getPlayer() instanceof Player player)) return;
        if (!current.isPlayer(player)) return;

        if (event.getInventory().getType() == InventoryType.ENDER_CHEST) {
            event.setCancelled(true);
            player.sendMessage("You are not allowed to open Ender Chests during the Tag event!");
        }
    }
}
