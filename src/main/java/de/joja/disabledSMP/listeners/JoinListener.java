package de.joja.disabledSMP.listeners;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.storage.YamlDisabilityStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.disablities.Disability.DISABILITIES_TOTAL_AMOUNT;
import static de.joja.disabledSMP.utils.Utils.random;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // if player disabilities are already loaded, then return
        if (plugin.disabilityMap.get(uuid) != null)
            return;

        // if player disabilities are not already loaded, but do exist, then load them
        List<Disability> disabilities = YamlDisabilityStorage.loadDisabilities(uuid);
        if (disabilities != null) {
            plugin.disabilityMap.put(uuid, disabilities);
            int allergy = YamlDisabilityStorage.loadAllergy(uuid);
            plugin.allergyMap.put(uuid, allergy);
            return;
        }

        // disabilities do not exist yet, it is a new player, then generate their disabilities
        List<Disability> possibleDisabilities = new ArrayList<>(Arrays.asList(Disability.values()));
        List<Disability> generatedDisabilities = new ArrayList<>(DISABILITIES_TOTAL_AMOUNT);

        for (int i = 0; i < DISABILITIES_TOTAL_AMOUNT/2; i++) {
            int randomIndex = random.nextInt(possibleDisabilities.size());
            generatedDisabilities.add(possibleDisabilities.get(randomIndex));
            possibleDisabilities.remove(randomIndex);
        }

        plugin.disabilityMap.put(uuid, generatedDisabilities);
    }

}
