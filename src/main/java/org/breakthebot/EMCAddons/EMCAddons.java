package org.breakthebot.EMCAddons;

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
