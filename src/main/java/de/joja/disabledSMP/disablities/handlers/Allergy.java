package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.DisabledSMP;
import de.joja.disabledSMP.advancements.AdvancementManager;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.AllergyMenu;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.listeners.DeathMessageListener;
import de.joja.disabledSMP.storage.YamlDisabilityStorage;
import de.joja.disabledSMP.utils.DConfig;
import org.bukkit.*;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.Utils.random;

public class Allergy extends DisabilityHandler {

    /** Everything players can be allergic to: each player gets one of these groups. */
    public static final Material[][] ALLERGY_TYPES = {
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

        this.specificInfoDialog = AllergyMenu::build;

        // checked often and the effects only last a moment longer than that, so they show up as soon as the player
        // gets close to an allergen and are gone right after leaving; the damage still only hits once per second
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();

                boolean reacting = hasDis(player, Disability.ALLERGY) && isNearAllergen(player);
                if (reacting) {
                    onAllergicReaction(player, now);
                } else if (reacting(uuid)) {
                    clearReaction(player);
                }
            }
        }, CHECK_TICKS, CHECK_TICKS);

    }

    /** How often the surroundings are checked, and how long the effects last (a little longer than that). */
    private static final long CHECK_TICKS = 4;
    private static final int EFFECT_TICKS = 8;
    private static final long DAMAGE_INTERVAL_MS = 1000;

    /** When the player was last hurt by an allergic reaction: only players that are reacting right now are in here. */
    private final Map<UUID, Long> lastDamage = new HashMap<>();

    private boolean reacting(UUID uuid) {
        return lastDamage.containsKey(uuid);
    }

    private boolean isNearAllergen(Player player) {
        UUID uuid = player.getUniqueId();
        if (plugin.allergyMap.get(uuid) == null)
            return false;

        for (Material allergyItem : ALLERGY_TYPES[plugin.allergyMap.get(uuid)])
            if (player.getInventory().contains(allergyItem))
                return true;

        double r = DConfig.ALLERGY_RADIUS;
        for (Entity entity : player.getNearbyEntities(r, r, r))
            if (entity instanceof Item item && isPlayerAllergicTo(item.getItemStack().getType(), uuid))
                return true;
        return false;
    }

    void onAllergicReaction(Player player, long now) {
        plugin.advancementManager.grant(player, AdvancementManager.ACHOO);

        player.addPotionEffect(PotionEffectType.POISON.createEffect(EFFECT_TICKS, 2));
        player.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(EFFECT_TICKS, 1));
        player.addPotionEffect(PotionEffectType.WEAKNESS.createEffect(EFFECT_TICKS, 1));
        player.addPotionEffect(PotionEffectType.MINING_FATIGUE.createEffect(EFFECT_TICKS, 2));

        Long last = lastDamage.get(player.getUniqueId());
        if (last == null || now - last >= DAMAGE_INTERVAL_MS) {
            lastDamage.put(player.getUniqueId(), now);
            DeathMessageListener.markAllergy(player);
            player.damage(2.5, DamageSource.builder(DamageType.WITHER).build());
        }
    }

    /** Away from the allergen: the effects go right away (only ours, not longer ones from potions). */
    private void clearReaction(Player player) {
        lastDamage.remove(player.getUniqueId());
        for (PotionEffectType type : List.of(PotionEffectType.POISON, PotionEffectType.SLOWNESS,
                PotionEffectType.WEAKNESS, PotionEffectType.MINING_FATIGUE)) {
            PotionEffect effect = player.getPotionEffect(type);
            if (effect != null && effect.getDuration() <= EFFECT_TICKS)
                player.removePotionEffect(type);
        }
    }

    boolean isPlayerAllergicTo(Material item, UUID uuid) {
        int allergyID = plugin.allergyMap.get(uuid);
        for (Material allergyItem : ALLERGY_TYPES[allergyID])
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
            int randomAllergyID = random.nextInt(ALLERGY_TYPES.length);
            YamlDisabilityStorage.saveAllergy(player.getUniqueId(), randomAllergyID);
            plugin.allergyMap.put(player.getUniqueId(), randomAllergyID);
        } else {
            plugin.allergyMap.put(player.getUniqueId(), loadedAllergyID);
        }
    }


}
