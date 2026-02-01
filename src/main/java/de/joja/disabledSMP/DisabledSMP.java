package de.joja.disabledSMP;

import de.joja.disabledSMP.commands.AddDisabilityCommand;
import de.joja.disabledSMP.commands.DisMenuCommand;
import de.joja.disabledSMP.commands.ListDisabilitiesCommand;
import de.joja.disabledSMP.commands.RemoveDisabilityCommand;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.MenuManager;
import de.joja.disabledSMP.listeners.CureListener;
import de.joja.disabledSMP.listeners.JoinListener;
import de.joja.disabledSMP.listeners.KillListener;
import de.joja.disabledSMP.storage.YamlDisabilityStorage;
import org.bukkit.Material;
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

        getServer().getPluginManager().registerEvents(new MenuManager(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new KillListener(), this);
        getServer().getPluginManager().registerEvents(new CureListener(), this);

        getCommand("add_disability").setExecutor(new AddDisabilityCommand());
        getCommand("remove_disability").setExecutor(new RemoveDisabilityCommand());
        getCommand("list_disabilities").setExecutor(new ListDisabilitiesCommand());
        getCommand("disabled_menu").setExecutor(new DisMenuCommand());
    }

    @Override
    public void onDisable() {
        disabilityMap.forEach(
                (uuid, disabilities) -> YamlDisabilityStorage
                        .saveDisabilities(uuid, disabilities.toArray(Disability[]::new), allergyMap.get(uuid))
        );
    }
}
