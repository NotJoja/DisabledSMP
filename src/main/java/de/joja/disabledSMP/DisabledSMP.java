package de.joja.disabledSMP;

import de.joja.disabledSMP.commands.*;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.DisabilityManager;
import de.joja.disabledSMP.listeners.JoinListener;
import de.joja.disabledSMP.listeners.KillListener;
import de.joja.disabledSMP.listeners.MenuListener;
import de.joja.disabledSMP.storage.YamlDisabilityStorage;
import org.bukkit.plugin.java.JavaPlugin;

public final class DisabledSMP extends JavaPlugin {

    public static DisabledSMP plugin;

    public DisabilityManager disManager = new DisabilityManager();

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {

        plugin = this;

        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new KillListener(), this);

        getCommand("add_disability").setExecutor(new AddDisabilityCommand());
        getCommand("remove_disability").setExecutor(new RemoveDisabilityCommand());
        getCommand("list_disabilities").setExecutor(new ListDisabilitiesCommand());
        getCommand("disabled_menu").setExecutor(new DisMenuCommand());
        getCommand("all_disabilities").setExecutor(new AllDisCommand());
    }

    @Override
    public void onDisable() {
        disManager.disabilityMap.forEach(
                (uuid, disabilities) ->
                        YamlDisabilityStorage.saveDisabilities(this, uuid, disabilities.toArray(Disability[]::new))
        );
    }
}
