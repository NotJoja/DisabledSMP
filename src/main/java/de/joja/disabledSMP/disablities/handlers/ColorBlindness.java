package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.utils.ResourcePacks;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.ColorblindMenu;
import de.joja.disabledSMP.storage.YamlDisabilityStorage;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ColorBlindness extends DisabilityHandler {

    public ColorBlindness() {
        this.enName = "Color Blindness";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You can't see colors"
        );

        this.deName = "Farbenblindheit";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du siehst keine Farben"
        );

        initIcons();

        this.specificInfoDialog = ColorblindMenu::build;
    }

    /** The two looks of the colorblind pack. Chosen per player in /dis menu -> Color Blindness. */
    public enum Mode {
        /** Ores look like coal, tools and armor like iron, colored blocks are gray. */
        SELECTIVE,
        /** Everything is black and white. */
        CLASSIC
    }

    private final Map<UUID, Mode> modes = new HashMap<>();

    public Mode getMode(Player player) {
        return modes.computeIfAbsent(player.getUniqueId(), uuid -> {
            String saved = YamlDisabilityStorage.loadColorblindMode(uuid);
            try {
                return saved == null ? Mode.SELECTIVE : Mode.valueOf(saved);
            } catch (IllegalArgumentException e) {
                return Mode.SELECTIVE;
            }
        });
    }

    /** Saves the choice, and swaps the pack right away if the player is colorblind. */
    public void setMode(Player player, Mode mode) {
        if (getMode(player) == mode)
            return;

        modes.put(player.getUniqueId(), mode);
        YamlDisabilityStorage.saveColorblindMode(player.getUniqueId(), mode.name());

        if (hasDis(player, Disability.COLOR_BLINDNESS)) {
            removeFromPlayer(player);
            addToPlayer(player);
        }
    }

    private static ResourcePacks.ResourcePack packOf(Mode mode) {
        return mode == Mode.CLASSIC ? ResourcePacks.COLORBLIND_CLASSIC : ResourcePacks.COLORBLIND;
    }

    /** The colorblind pack is added on top of the main pack (which everyone has), not instead of it. */
    @Override
    public void addToPlayer(Player player) {
        packOf(getMode(player)).send(player);
    }

    @Override
    public void removeFromPlayer(Player player) {
        ResourcePacks.COLORBLIND.remove(player);
        ResourcePacks.COLORBLIND_CLASSIC.remove(player);
    }
}
