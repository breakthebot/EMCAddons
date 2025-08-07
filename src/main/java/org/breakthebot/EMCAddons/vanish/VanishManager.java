package org.breakthebot.EMCAddons.vanish;

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

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;


public class VanishManager implements Listener {
    private static Plugin plugin;

    public static void init(Plugin pluginInstance) {
        plugin = pluginInstance;
    }

    private static boolean checkImmune(Player player) {
        return player.hasPermission("ly.skynet.see.specvanish");
    }

    private static boolean checkAllowed(Player player) {
        return player.hasPermission("ly.skynet.specvanish");
    }

    private static boolean checkVanished(Player player) {
        return checkAllowed(player) && player.getGameMode().equals(GameMode.SPECTATOR);
    }

    public static void vanish(Player staff) {
        if (!checkAllowed(staff)) {
            staff.sendMessage("§cYou do not have permission to go into vanish.");
            return;
        }
        int total = 0;
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!checkImmune(other)) {
                other.hidePlayer(plugin, staff);
                total++;
            }
        }
        staff.sendMessage("§aYou are now vanished to " + total + " players.");
    }

    public static void reveal(Player staff) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, staff);
        }
        staff.sendMessage("§aYou are now visible to players.");
    }

    public static void handleJoin(Player player) {
        if (checkVanished(player)) {
            vanish(player);
        }
        if (checkImmune(player)) { return; }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            if (checkVanished(online)) {
                player.hidePlayer(plugin, online);
            }
        }
    }

    @EventHandler
    public void onGameMode(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode oldMode = player.getGameMode();
        GameMode newMode = event.getNewGameMode();

        if (newMode == GameMode.SPECTATOR) {
            VanishManager.vanish(player);
        } else if (oldMode == GameMode.SPECTATOR) {
            VanishManager.reveal(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        VanishManager.handleJoin(event.getPlayer());
    }
}
