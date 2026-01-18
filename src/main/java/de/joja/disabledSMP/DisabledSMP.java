package de.joja.disabledSMP;

import de.joja.disabledSMP.disablities.DisabilityManager;
import de.joja.disabledSMP.listeners.JoinListener;
import de.joja.disabledSMP.storage.YamlDisabilityStorage;
import de.joja.disabledSMP.utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;

public final class DisabledSMP extends JavaPlugin {

    public DisabilityManager disabilityM = new DisabilityManager();

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
    }

    @Override
    public void onDisable() {
        disabilityM.disabilityMap.forEach(
                (uuid, disabilityIdList) ->
                        YamlDisabilityStorage.saveDisabilityIDs(this, uuid, Utils.asIntArray(disabilityIdList))
        );
    }
}
