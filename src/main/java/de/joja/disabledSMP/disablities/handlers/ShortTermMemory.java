package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class ShortTermMemory extends DisabilityHandler {

    public ShortTermMemory() {
        this.enName = "Short-Term Memory";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "Inventory items may temporarily change",
                "their name, texture or position"
        );

        this.deName = "Kurzzeitgedächtnis";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Items in deinem Inventar können temporär",
                "Aussehen, Namen oder Position ändern"
        );

        initIcons();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!hasDis(player, Disability.SHORT_TERM_MEMORY))
                    continue;

                if (random.nextFloat() < 0.2)
                    randomizeName(player, random.nextInt(36), 6000);
                if (random.nextFloat() < 0.15)
                    shuffleInventory(player);
                if (random.nextFloat() < 0.12)
                    randomizeTexture(player, 6000);

            }
        }, 20L, 100L);
    }

    private final Random random = new Random();

    private final List<String> words = List.of(
            "holy", "bear", "golden", "cursed", "diamond",
            "whisper", "forbidden", "pickle", "cosmic", "french",
            "ancient", "suspicious", "tiny", "explosive", "axe",
            "eternal", "banana", "shadow", "glorious", "bird",
            "wet", "aggressive", "royal", "forgotten", "godly",
            "bread", "blood", "gun", "dirt", "dog", "cat", "hay", "gay",
            "fish", "four", "five", "runner", "sword", "efw4biurwefgbhib", "banana",
            "weapon", "spoon", "food", "knife", "stone", "rock", "iron", "furry", "boots", "head",
            "norway", "russian", "gold", "ingot", "anus", "cock", "cow", "mum", "daddy", "dominant",
            "husband", "pickaxe", "bow", "hammer", "amazing", "grand", "unimaginable", "racist", "TNT",
            "kid", "keyboard", "table", "chair", "book", "carrot", "cake", "8ygubr3ewiouly", "block", "nugget",
            "wooden", "soap", "card", "paper", "spiky", "dangerous", "hole", "tom", "adolf", "enormous", "joja",
            "black", "power", "hate", "button", "door", "hoe", "whore", "burning", "sand", "orbital cannon", "penetrated",
            "coal", "trident", "meth", "spawn egg", "armor", "mace"
    );

    private String randomName(int minWords, int maxWords) {
        List<String> shuffled = new ArrayList<>(words);
        Collections.shuffle(shuffled, random);

        int count = minWords + random.nextInt(maxWords - minWords + 1);
        count = Math.min(count, shuffled.size());

        return String.join(" ", shuffled.subList(0, count));
    }

    public void randomizeName(Player player, int slot, long duration) {
        ItemStack original = player.getInventory().getItem(slot);

        if (original == null || original.getType().isAir())
            return;

        original = original.clone();

        ItemStack changed = original.clone();
        ItemMeta meta = changed.getItemMeta();

        meta.setDisplayName(randomName(2, 4));
        changed.setItemMeta(meta);

        final ItemStack backup = original;
        player.getInventory().setItem(slot, changed);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ItemStack current = player.getInventory().getItem(slot);

            if (current != null && current.isSimilar(changed)) {
                player.getInventory().setItem(slot, backup);
            }
        }, duration);
    }

    public void shuffleInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        int a = random.nextInt(36);
        int b = random.nextInt(36);

        if (a <= 8 || b <= 8)
            if (random.nextDouble() > 0.35)
                return;

        ItemStack itemA = inv.getItem(a);
        ItemStack itemB = inv.getItem(b);
        inv.setItem(a, itemB);
        inv.setItem(b, itemA);
    }

    public void randomizeTexture(Player player, long duration) {

        PlayerInventory inv = player.getInventory();
        List<Integer> occupiedSlots = new ArrayList<>();
        ItemStack[] contents = inv.getStorageContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];

            if (item != null && !item.getType().isAir()) {
                occupiedSlots.add(i);
            }
        }

        if (occupiedSlots.isEmpty())
            return;

        int slot = occupiedSlots.get(random.nextInt(occupiedSlots.size()));

        ItemStack original = inv.getItem(slot).clone();

        List<Material> items = Arrays.stream(Material.values())
                .filter(Material::isItem)
                .filter(m -> !m.isAir())
                .filter(m -> !m.isLegacy())
                .toList();

        if (items.isEmpty())
            return;

        Material target = items.get(random.nextInt(items.size()));

        ItemStack changed = original.clone();
        ItemMeta meta = changed.getItemMeta();

        meta.setItemModel(NamespacedKey.minecraft(
                target.name().toLowerCase(Locale.ROOT)
        ));

        changed.setItemMeta(meta);
        inv.setItem(slot, changed);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ItemStack current = inv.getItem(slot);

            if (current != null && current.isSimilar(changed)) {
                inv.setItem(slot, original);
            }
        }, duration);
    }


}
