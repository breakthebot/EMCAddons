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

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PlayerChangePlotEvent;
import com.palmergames.bukkit.towny.event.SpawnEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.breakthebot.EMCAddons.events.MainUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;


public class HideListeners implements Listener {
    
    private static final Map<UUID, Long> pendingDisqualification = new HashMap<>();
    private static final Map<UUID, Long> recentRejoins = new HashMap<>();
    private static final Map<UUID, List<ItemStack>> respawnGold = new HashMap<>();

    public static void clearArrays() {
        pendingDisqualification.clear();
        recentRejoins.clear();
        respawnGold.clear();
    }

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

        HideNSeek current = HideUtils.getCurrentEvent();
        if (current == null) return;
        if (current.isHunter(attacker)) return;

        TownyAPI API = TownyAPI.getInstance();
        Town town1 = API.getTown(victim.getLocation());
        Town town2 = API.getTown(attacker.getLocation());
        if (town1 == null || !town1.equals(town2)) return;
        if (town1.equals(current.getHostTown())) {
            event.setCancelled(true);
            attacker.sendMessage(Component.text("You may not attack this player as you are not a hunter in this Event.").color(NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onLogOff(PlayerQuitEvent event) {
        HideNSeek current = HideUtils.getCurrentEvent();
        Player player = event.getPlayer();
        if (current == null) return;
        if (!current.isPlayer(player)) return;
        UUID uuid = player.getUniqueId();
        pendingDisqualification.put(uuid, System.currentTimeMillis());
    }

    public static void handleDisqualified(UUID uuid) {
        HideNSeek current = HideUtils.getCurrentEvent();
        if (current == null) { return; }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }

        pendingDisqualification.remove(player.getUniqueId());

        current.removePlayer(player);
        current.addDisqualified(player);

        if (!player.isDead()) {
            player.setHealth(0.0);
        }

        HideUtils.broadcastPlayers(current, player.getName() + " was disqualified!");
        HideUtils.broadcastHunters(current, player.getName() + " was disqualified!");

        Location location = player.getLocation();
        location.getWorld().strikeLightningEffect(location);

        player.sendMessage(Component.text("You have been disqualified from the Hide & Seek event. If you believe this is a mistake, contact event organizers.").color(NamedTextColor.RED));

        try {
            TownyAPI.getInstance().requestTeleport(player, current.getHostTown().getSpawn(), 0);
        } catch (TownyException e) {
            MainUtils.broadcastAdmins("Warning! Could not teleport " + player.getName() + " to Town spawn!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        HideNSeek current = HideUtils.getCurrentEvent();
        if (current == null) { return; }

        Player player = event.getPlayer();
        if (!current.isPlayer(player)) { return; }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (recentRejoins.containsKey(uuid)) {
            long lastJoin = recentRejoins.get(uuid);
            if (now - lastJoin <= 5 * 60 * 1000) {
                HideUtils.broadcastHunters(current, player.getName() + " has logged on twice in the past 5 minutes. \nUse /em player disqualify if caught cheating.");
            }
        }
        recentRejoins.put(uuid, now);

        Long logoff = pendingDisqualification.get(uuid);
        if (logoff != null) {
            if (logoff < now - 60_000L) {
                handleDisqualified(uuid);
            } else {
                pendingDisqualification.remove(uuid);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        HideNSeek current = HideUtils.getCurrentEvent();
        if (current == null) return;
        if (!current.isPlayer(player)) return;
        if (!current.isDisqualified(player))  {
            handleDisqualified(player.getUniqueId());
        }

        refundGold(event);
    }

    public void refundGold(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> drops = event.getDrops();
        List<ItemStack> savedGold = new ArrayList<>();

        Iterator<ItemStack> iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (item.getType() == Material.GOLD_INGOT ||
                    item.getType() == Material.GOLD_BLOCK ||
                    item.getType() == Material.GOLD_NUGGET) {

                savedGold.add(item.clone());
                iterator.remove();
            }
        }
        if (!savedGold.isEmpty()) {
            respawnGold.put(player.getUniqueId(), savedGold);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (respawnGold.containsKey(uuid)) {
            List<ItemStack> refund = respawnGold.remove(uuid);
            for (ItemStack gold : refund) {
                player.getInventory().addItem(gold);
            }
        }
    }

    @EventHandler
    public void onTeleport(SpawnEvent event) {
        HideNSeek current = HideUtils.getCurrentEvent();
        if (current == null) return;
        if (!current.isPlayer(event.getPlayer())) return;
        Town town = TownyAPI.getInstance().getTown(event.getFrom());
        if (town == null) return;
        if (!current.getHostTown().equals(town)) return;

        event.setCancelled(true);
        event.setCancelMessage("You are not allowed to teleport away while participating in the Hide & Seek event!");
    }

    @EventHandler
    public void onPlotChange(PlayerChangePlotEvent event) {
        HideNSeek current = HideUtils.getCurrentEvent();
        if (current == null) return;
        if (!current.isPlayer(event.getPlayer())) return;
        TownBlock origin = event.getFrom().getTownBlockOrNull();
        if (origin == null) return;
        Town town = origin.getTownOrNull();
        if (town == null) return;

        TownBlock targetBlock = event.getTo().getTownBlockOrNull();
        Town targetTown = null;
        if (targetBlock != null) { targetTown = targetBlock.getTownOrNull(); }
        if (town.equals(targetTown)) return;
        if (current.getHostTown().equals(town)) {
            handleDisqualified(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onECOpen(InventoryOpenEvent event) {
        HideNSeek current = HideUtils.getCurrentEvent();
        if (current == null) return;

        if (!(event.getPlayer() instanceof Player player)) return;
        if (!current.isPlayer(player)) return;

        if (event.getInventory().getType() == InventoryType.ENDER_CHEST) {
            event.setCancelled(true);
            player.sendMessage("You are not allowed to open Ender Chests during the Hide & Seek event!");
        }
    }
}