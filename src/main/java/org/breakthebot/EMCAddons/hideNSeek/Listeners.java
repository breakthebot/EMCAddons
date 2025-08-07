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
import com.palmergames.bukkit.towny.event.PlayerChangePlotEvent;
import com.palmergames.bukkit.towny.event.SpawnEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.breakthebot.EMCAddons.EMCAddons;
import org.breakthebot.EMCAddons.events.EventManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class Listeners implements Listener {

    private static @Nullable HideNSeek getCurrentEvent() {
        return EventManager.getInstance().getCurrent();
    }

    private static final Set<UUID> pendingDisqualification = new HashSet<>();
    private static final Map<UUID, Long> recentRejoins = new HashMap<>();
    private static final Set<UUID> waitingLogin = new HashSet<>();
    private static final Map<UUID, List<ItemStack>> respawnGold = new HashMap<>();

    public static void clearArrays() {
        pendingDisqualification.clear();
        recentRejoins.clear();
        waitingLogin.clear();
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

        HideNSeek current = getCurrentEvent();
        if (current == null) return;
        if (current.getHunters().contains(attacker)) return;

        TownyAPI API = TownyAPI.getInstance();
        Town town1 = API.getTown(victim.getLocation());
        Town town2 = API.getTown(attacker.getLocation());
        if (town1 == null || !town1.equals(town2)) return;
        if (town1.equals(current.getHostTown())) {
            event.setCancelled(true);
            TownyMessaging.sendErrorMsg(attacker, "You may not attack this player as you are not a hunter in this Event.");
        }
    }

    @EventHandler
    public void onLogOff(PlayerQuitEvent event) {
        HideNSeek current = getCurrentEvent();
        Player player = event.getPlayer();
        if (current == null) return;
        if (!current.getPlayers().contains(player)) return;
        UUID uuid = player.getUniqueId();
        pendingDisqualification.add(uuid);

        EMCAddons.getInstance().runTaskDelayed(player, () -> {
            if (pendingDisqualification.contains(uuid)) {
                handleDisqualified(player);
            }
        }, 20L * 60);
    }

    public static void handleDisqualified(Player player) {
        if (!player.isOnline()) {
            waitingLogin.add(player.getUniqueId());
            return;
        }

        HideNSeek current = getCurrentEvent();
        if (current == null) { return; }

        pendingDisqualification.remove(player.getUniqueId());

        List<Player> disqualifiedList = current.getDisqualified();
        disqualifiedList.add(player);
        current.setDisqualified(disqualifiedList);

        List<Player> playerList = current.getPlayers();
        playerList.remove(player);
        current.setPlayers(playerList);

        player.setHealth(0.0);

        utils.broadcastPlayers(current, player.getName() + " was disqualified!");
        utils.broadcastHunters(current, player.getName() + " was disqualified!");

        try {
            TownyAPI.getInstance().requestTeleport(player, current.getHostTown().getSpawn(), 0);
            player.sendMessage(Component.text("You have been disqualified from the Hide & Seek event. If you believe this is a mistake, contact event organizers.").color(NamedTextColor.RED));
        } catch (TownyException e) {
            utils.broadcastHunters(current, "Warning! Could not teleport " + player.getName() + " to Town spawn!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        HideNSeek current = getCurrentEvent();
        if (current == null) { return; }

        Player player = event.getPlayer();
        if (!current.getPlayers().contains(player)) { return; }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (recentRejoins.containsKey(uuid)) {
            long lastJoin = recentRejoins.get(uuid);
            if (now - lastJoin <= 5 * 60 * 1000) {
                utils.broadcastHunters(current, player.getName() + " has logged on twice in the past 5 minutes. \nUse /em player disqualify if caught cheating.");
            }
        }
        recentRejoins.put(uuid, now);

        if (waitingLogin.remove(player.getUniqueId())) {
            handleDisqualified(player);
        } else {
            pendingDisqualification.remove(uuid);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        HideNSeek current = getCurrentEvent();
        if (current == null) return;
        if (!current.getPlayers().contains(player)) return;

        Town deathTown = TownyAPI.getInstance().getTown(player.getLocation());
        if (deathTown == null || !deathTown.equals(current.getHostTown())) return;

        Location deathLocation = player.getLocation();
        deathLocation.getWorld().strikeLightningEffect(deathLocation);


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
        HideNSeek current = getCurrentEvent();
        if (current == null) return;
        if (!current.getPlayers().contains(event.getPlayer())) return;
        Town town = TownyAPI.getInstance().getTown(event.getFrom());
        if (town == null) return;
        if (!current.getHostTown().equals(town)) return;

        event.setCancelled(true);
        event.setCancelMessage("You are not allowed to teleport away while participating in the Hide & Seek event!");
    }

    @EventHandler
    public void onPlotChange(PlayerChangePlotEvent event) {
        HideNSeek current = getCurrentEvent();
        if (current == null) return;
        if (!utils.isPlayer(event.getPlayer())) return;
        TownBlock origin = event.getFrom().getTownBlockOrNull();
        if (origin == null) return;
        Town town = origin.getTownOrNull();
        if (town == null) return;

        TownBlock targetBlock = event.getTo().getTownBlockOrNull();
        Town targetTown = null;
        if (targetBlock != null) { targetTown = targetBlock.getTownOrNull(); }
        if (town.equals(targetTown)) return;
        if (current.getHostTown().equals(town)) {
            handleDisqualified(event.getPlayer());
        }
    }

    @EventHandler
    public void onECOpen(PlayerInteractEvent event) {
        HideNSeek current = getCurrentEvent();
        if (current == null) return;
        if (!current.getPlayers().contains(event.getPlayer())) return;
        Town town = TownyAPI.getInstance().getTown(event.getInteractionPoint());
        if (town == null) return;
        if (!current.getHostTown().equals(town)) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ENDER_CHEST) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage("You are not allowed to open Ender Chests during the Hide & Seek event!");
    }
}