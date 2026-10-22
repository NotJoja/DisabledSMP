package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.DisabledSMP;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.storage.YamlDisabilityStorage;
import org.bukkit.*;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.Utils.random;

public class Allergy extends DisabilityHandler {

    Material[][] allergyTypes = {
            {Material.APPLE},
            {Material.POTATO, Material.BAKED_POTATO, Material.POISONOUS_POTATO},
            {Material.SWEET_BERRIES},
            {Material.WHEAT, Material.HAY_BLOCK},
            {Material.CARROT},
            {Material.MELON, Material.MELON_SLICE},
            {Material.BEETROOT, Material.BEETROOT_SOUP},
            {Material.COCOA_BEANS, Material.COOKIE},
            {Material.PUMPKIN, Material.CARVED_PUMPKIN, Material.PUMPKIN_PIE},
            {Material.MILK_BUCKET},
            {Material.SUGAR_CANE},
            {Material.BROWN_MUSHROOM, Material.RED_MUSHROOM},
            {Material.EGG, Material.BROWN_EGG, Material.BLUE_EGG},
            {Material.ROSE_BUSH, Material.LILAC, Material.PEONY},
            {Material.MOSS_BLOCK, Material.MOSS_CARPET}
    };

    public Allergy() {
        this.enName = "Allergy";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "A certain food will harm",
                "and weaken you nearby"
        );

        this.deName = "Allergie";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Ein bestimmtes Essen schadet",
                "dir stark in deiner Nähe"
        );

        initIcons();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!hasDis(player, Disability.ALLERGY))
                    continue;

                for (Entity entity : player.getNearbyEntities(8, 8, 8)) {

                    if (entity instanceof Item item) {
                        Material ieMat = item.getItemStack().getType();
                        if (isPlayerAllergicTo(ieMat, player.getUniqueId()))
                            onAllergicReactionSecond(player);
                    }
                }

                int allergyID = plugin.allergyMap.get(player.getUniqueId());
                for (Material allergyItem : allergyTypes[allergyID])
                    if (player.getInventory().contains(allergyItem))
                        onAllergicReactionSecond(player);
            }
        }, 20L, 20L);

    }

    void onAllergicReactionSecond(Player player) {

        player.addPotionEffect(PotionEffectType.POISON.createEffect(40, 2));
        player.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(40, 1));
        player.addPotionEffect(PotionEffectType.WEAKNESS.createEffect(40, 1));
        player.addPotionEffect(PotionEffectType.MINING_FATIGUE.createEffect(40, 2));
        player.damage(2.5, DamageSource.builder(DamageType.WITHER).build());
    }

    boolean isPlayerAllergicTo(Material item, UUID uuid) {
        int allergyID = plugin.allergyMap.get(uuid);
        for (Material allergyItem : allergyTypes[allergyID])
            if (item == allergyItem)
                return true;
        return false;
    }

    @Override
    public void addToPlayer(Player player) {
        if (plugin.allergyMap.get(player.getUniqueId()) != null)
            return;

        int loadedAllergyID = YamlDisabilityStorage.loadAllergy(player.getUniqueId());
        if (loadedAllergyID == -1) {
            int randomAllergyID = random.nextInt(allergyTypes.length);
            YamlDisabilityStorage.saveAllergy(player.getUniqueId(), randomAllergyID);
            plugin.allergyMap.put(player.getUniqueId(), randomAllergyID);
        } else {
            plugin.allergyMap.put(player.getUniqueId(), loadedAllergyID);
        }
    }


}
