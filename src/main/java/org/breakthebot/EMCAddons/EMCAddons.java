package org.breakthebot.EMCAddons;

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

import org.breakthebot.EMCAddons.events.command;
import org.breakthebot.EMCAddons.events.manager;
import org.breakthebot.EMCAddons.vanish.VanishManager;
import org.breakthebot.EMCAddons.vanish.listener;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class EMCAddons extends JavaPlugin {
    private static EMCAddons instance;

    public static EMCAddons getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Plugin started!");
        VanishManager.init(this);
        getServer().getPluginManager().registerEvents(new listener(), this);

        getCommand("eventmanager").setExecutor(new command());
        getCommand("eventmanager").setTabCompleter(new command());

        manager.currentEvents.add("hideNSeek");
    }

    public void eventRegister(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, instance);
    }

    public void eventUnregister(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin shutdown");
    }
}
