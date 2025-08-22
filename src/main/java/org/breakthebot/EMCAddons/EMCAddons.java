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

import org.breakthebot.EMCAddons.events.MainCMD;
import org.breakthebot.EMCAddons.events.hideNSeek.HideCMDAlias;
import org.breakthebot.EMCAddons.events.tag.TagCMDAlias;
import org.breakthebot.EMCAddons.superbreaker.superCMD;
import org.breakthebot.EMCAddons.superbreaker.superUtils;
import org.breakthebot.EMCAddons.vanish.VanishListeners;
import org.breakthebot.EMCAddons.vanish.VanishManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
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
        addListener(new VanishListeners());
        addListener(new superUtils());

        MainCMD mainCMD = new MainCMD();
        commandRegister("eventmanager", mainCMD, mainCMD);

        HideCMDAlias hideCMDAlias = new HideCMDAlias();
        commandRegister("hide", hideCMDAlias, hideCMDAlias);

        TagCMDAlias tagCMDAlias = new TagCMDAlias();
        commandRegister("tag", tagCMDAlias, tagCMDAlias);

        superCMD superCMD = new superCMD();
        commandRegister("superbreak", superCMD, superCMD);
    }

    private void commandRegister(String name, CommandExecutor executor, TabCompleter tabCompleter) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            getLogger().warning("Could not find command: " + name);
            return;
        }
        cmd.setExecutor(executor);
        cmd.setTabCompleter(tabCompleter);
    }

    public void addListener(Listener listener) { getServer().getPluginManager().registerEvents(listener, instance); }

    public void removeListener(Listener listener) { HandlerList.unregisterAll(listener); }

    @Override
    public void onDisable() {
        getLogger().info("Plugin shutdown");
    }
}
