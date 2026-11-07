package de.joja.disabledSMP.listeners;

import de.joja.disabledSMP.utils.DConfig;
import de.joja.disabledSMP.utils.ResourcePacks;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.storage.YamlDisabilityStorage;
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

        ResourcePacks.MAIN.send(player);

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
            return;
        }

        // disabilities do not exist yet, it is a new player, then generate their disabilities
        List<Disability> possibleDisabilities = new ArrayList<>(Arrays.asList(Disability.values()));
        List<Disability> generatedDisabilities = new ArrayList<>(DISABILITIES_TOTAL_AMOUNT);

        int amount = firstJoinAmount();
        DConfig.debug(player.getName() + " is new, gets " + amount + " disabilities");

        for (int i = 0; i < amount; i++) {
            int randomIndex = random.nextInt(possibleDisabilities.size());
            generatedDisabilities.add(possibleDisabilities.get(randomIndex));
            possibleDisabilities.remove(randomIndex);
        }

        plugin.disabilityMap.put(uuid, generatedDisabilities);

        for (Disability dis : generatedDisabilities)
            dis.handler.addToPlayer(player);
    }


    /** How many disabilities a new player gets: the config value (-1 = half of all), within min and max. */
    private static int firstJoinAmount() {
        int amount = DConfig.FIRST_JOIN_DISABILITIES >= 0 ? DConfig.FIRST_JOIN_DISABILITIES : DISABILITIES_TOTAL_AMOUNT / 2;

        if (DConfig.MIN_DISABILITIES >= 0)
            amount = Math.max(amount, DConfig.MIN_DISABILITIES);
        if (DConfig.MAX_DISABILITIES >= 0)
            amount = Math.min(amount, DConfig.MAX_DISABILITIES);

        return Math.max(0, Math.min(amount, DISABILITIES_TOTAL_AMOUNT));
    }

}
