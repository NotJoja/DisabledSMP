package de.joja.disabledSMP.listeners;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.ItemUtils;
import de.joja.disabledSMP.storage.YamlDisabilityStorage;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.disablities.Disability.DISABILITIES_TOTAL_AMOUNT;
import static de.joja.disabledSMP.utils.Utils.random;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        byte[] hash = HexFormat.of().parseHex("7341e8f884a8f1117eb90c1393a39bf850fd29bf");

        player.setResourcePack(
                "https://example.com/dissmp-resourcepack.zip",
                hash,
                Component.text("This server requires the DisabledSMP resource pack"),
                true
        );

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

    @EventHandler
    public void onPackStatus(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED
                || event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {

            event.getPlayer().kick(
                    Component.text("You must accept the resource pack to play")
            );
        }
    }


}
