package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.utils.DConfig;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.utils.Utils.random;

public class MentallyDisabled extends DisabilityHandler {

    public MentallyDisabled() {
        this.enName = "Mentally Disabled";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
               "e"
        );

        this.deName = "Geistlich Behindert";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "e"
        );

        initIcons();
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {

//        if (!(event.getView().getPlayer() instanceof Player player))
//            return;
//
//        if (!hasDis(player, Disability.MENTALLY_DISABLED))
//
//        if (Math.random() > 0.2)
//            return;
//
//        ItemStack original = event.getInventory().getResult();
//        if (original == null) return;
//
//        ItemStack replacement = getRandomAlternative(original);
//
//        if (replacement != null) {
//            event.getInventory().setResult(replacement);
//        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {

        Player player = event.getPlayer();
        if (!hasDis(player, Disability.MENTALLY_DISABLED))
            return;

        if (Math.random() > 0.3)
            return;

        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        String[] words = message.split(" ");
        for (int i = 0; i < words.length; i++) {
            int randi = random.nextInt(words.length);
            String temp = words[i];
            words[i] = words[randi];
            words[randi] = temp;
        }

        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < words.length; i++)
            strBuilder.append(words[i]).append(" ");

        event.message(Component.text(strBuilder.toString()));
    }

    @EventHandler
    public void onFemboyChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID jojaUUID = UUID.fromString("13ad46862f814ea1bd776ef2d89f0964");
        String jojaName = "Joja_bings";
        if (player.getUniqueId().equals(jojaUUID) || player.getName().equals(jojaName))
            return;

        if (Math.random() > 0.01)
            return;

        if (DConfig.LANGUAGE.equals("en"))
            event.message(Component.text("I'm a horny femboy :3"));
        else if (DConfig.LANGUAGE.equals("de"))
            event.message(Component.text("Ich bin ein geiler femboy :3"));
    }




}
