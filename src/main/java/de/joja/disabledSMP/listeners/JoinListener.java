package de.joja.disabledSMP.listeners;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.storage.YamlDisabilityStorage;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.disablities.Disability.DISABILITIES_TOTAL_AMOUNT;
import static de.joja.disabledSMP.utils.Utils.random;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        player.setResourcePack(
                "https://raw.githubusercontent.com/NotJoja/DisabledSMP/main/src/main/resources/DisabledSMP.zip",
                null,
                Component.text("This server requires the DisabledSMP resource pack!"),
                true
        );

        // if player disabilities are already loaded, then return
        List<Disability> disabilities = plugin.disabilityMap.get(uuid);
        if (disabilities != null) {
            for (Disability dis : disabilities)
                dis.handler.addToPlayer(player);
            return;
        }

        // if player disabilities are not already loaded, but do exist, then load them
        disabilities = YamlDisabilityStorage.loadDisabilities(uuid);
        if (disabilities != null) {

            plugin.disabilityMap.put(uuid, disabilities);
            for (Disability dis : disabilities)
                dis.handler.addToPlayer(player);

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

        for (Disability dis : generatedDisabilities)
            dis.handler.addToPlayer(player);
    }


}
