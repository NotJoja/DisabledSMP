package de.joja.disabledSMP;

import org.bukkit.plugin.java.JavaPlugin;

public final class DisabledSMP extends JavaPlugin {

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(new FuckListener(), this);

        getCommand("goon").setExecutor();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

    }
}
