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

import org.breakthebot.EMCAddons.vanish.VanishManager;
import org.breakthebot.EMCAddons.vanish.events;
import org.bukkit.plugin.java.JavaPlugin;

public final class EMCAddons extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Plugin started!");
        VanishManager.init(this);
        getServer().getPluginManager().registerEvents(new events(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin shutdown");
    }
}
