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

<<<<<<< Updated upstream
<<<<<<< Updated upstream
=======
import org.breakthebot.EMCAddons.events.MainCMD;
>>>>>>> Stashed changes
=======
import org.breakthebot.EMCAddons.events.MainCMD;
>>>>>>> Stashed changes
import org.breakthebot.EMCAddons.vanish.VanishManager;
import org.breakthebot.EMCAddons.vanish.events;
import org.bukkit.plugin.java.JavaPlugin;

public final class EMCAddons extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Plugin started!");
        VanishManager.init(this);
<<<<<<< Updated upstream
        getServer().getPluginManager().registerEvents(new events(), this);
=======
        getServer().getPluginManager().registerEvents(new VanishManager(), this);

        getCommand("eventmanager").setExecutor(new MainCMD());
        getCommand("eventmanager").setTabCompleter(new MainCMD());

        detectFolia();
    }

    public void eventRegister(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, instance);
    }

    public void eventUnregister(Listener listener) {
        HandlerList.unregisterAll(listener);
>>>>>>> Stashed changes
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin shutdown");
    }
<<<<<<< Updated upstream
=======

    private void detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            getLogger().info("Folia environment detected.");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            getLogger().info("Running on standard Bukkit/Paper environment.");
            isFolia = false;
        }
    }

    public void runTaskDelayed(Player player, Runnable task, long duration) {
        if (isFolia) {
            player.getScheduler().runDelayed(this,
                    scheduledTask -> task.run(),
                    task,
                    duration
            );
        } else {
            Bukkit.getScheduler().runTaskLater(this, task, duration);
        }
    }
>>>>>>> Stashed changes
}
