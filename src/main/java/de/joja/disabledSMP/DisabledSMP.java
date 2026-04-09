package de.joja.disabledSMP;

import de.joja.disabledSMP.commands.DisCommand;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.MenuManager;
import de.joja.disabledSMP.listeners.CureListener;
import de.joja.disabledSMP.listeners.JoinListener;
import de.joja.disabledSMP.listeners.KillListener;
import de.joja.disabledSMP.storage.YamlDisabilityStorage;
import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DisabledSMP extends JavaPlugin {

    public static DisabledSMP plugin;

    public Map<UUID, List<Disability>> disabilityMap = new HashMap<>();
    public Map<UUID, Integer> allergyMap = new HashMap<>();

    public static final Material[] possibleAllergies = {};

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {

        plugin = this;

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new MenuManager(), this);
        pm.registerEvents(new JoinListener(), this);
        pm.registerEvents(new KillListener(), this);
        pm.registerEvents(new CureListener(), this);

        for (Disability dis : Disability.values())
            pm.registerEvents(dis.handler, this);

        // Register the new combined command
        getCommand("dis").setExecutor(new DisCommand());
        getCommand("dis").setTabCompleter(new DisCommand());

        // Remove old command registrations
        // getCommand("add_disability").setExecutor(new AddDisabilityCommand());
        // getCommand("remove_disability").setExecutor(new RemoveDisabilityCommand());
        // getCommand("list_disabilities").setExecutor(new ListDisabilitiesCommand());
        // getCommand("disabled_menu").setExecutor(new DisMenuCommand());
    }

    @Override
    public void onDisable() {
        disabilityMap.forEach(
                (uuid, disabilities) -> YamlDisabilityStorage
                        .saveDisabilities(uuid, disabilities.toArray(Disability[]::new), 0) //allergyMap.get(uuid))
        );
    }
}
