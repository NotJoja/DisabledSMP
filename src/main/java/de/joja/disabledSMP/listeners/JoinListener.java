package de.joja.disabledSMP.listeners;

import de.joja.disabledSMP.DisabledSMP;
import de.joja.disabledSMP.storage.YamlDisabilityStorage;
import de.joja.disabledSMP.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.Disability.DISABILITIES_TOTAL_AMOUNT;
import static de.joja.disabledSMP.utils.Utils.random;

public class JoinListener implements Listener {

    DisabledSMP plugin;

    public JoinListener(DisabledSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // if player disabilities are already loaded, then return
        if (plugin.disabilityM.disabilityMap.get(uuid) != null)
            return;

        // if player disabilities are not already loaded, but do exist, then load them
        int[] disabilityIDs = YamlDisabilityStorage.loadDisabilityIDs(plugin, uuid);
        if (disabilityIDs != null) {
            List<Integer> disabilityList = Utils.asIntegerList(disabilityIDs, DISABILITIES_TOTAL_AMOUNT);
            plugin.disabilityM.disabilityMap.put(uuid, disabilityList);
            return;
        }

        // disabilities do not exist yet, it is a new player, then generate their disabilities
        List<Integer> possibleDisabilityIDs = new ArrayList<>(DISABILITIES_TOTAL_AMOUNT);
        for (int i = 0; i < DISABILITIES_TOTAL_AMOUNT; i++)
            possibleDisabilityIDs.add(i);
        List<Integer> generatedDisabilityIDs = new ArrayList<>(DISABILITIES_TOTAL_AMOUNT);

        for (int i = 0; i < DISABILITIES_TOTAL_AMOUNT/2; i++) {
            int randomIndex = random.nextInt(possibleDisabilityIDs.size());
            generatedDisabilityIDs.add(possibleDisabilityIDs.get(randomIndex));
            possibleDisabilityIDs.remove(randomIndex);
        }

        plugin.disabilityM.disabilityMap.put(uuid, generatedDisabilityIDs);
    }

}
