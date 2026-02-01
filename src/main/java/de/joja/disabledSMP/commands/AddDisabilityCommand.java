package de.joja.disabledSMP.commands;

import de.joja.disabledSMP.disablities.Disability;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.disablities.Disability.DISABILITIES_TOTAL_AMOUNT;

public class AddDisabilityCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length < 2) {
            sender.sendRichMessage("<red>Not enough args!");
            return true;
        }

        if (args.length > 2) {
            sender.sendRichMessage("<red>Too many args!");
            return true;
        }

        Player player = plugin.getServer().getPlayer(args[0]);
        if (player == null) {
            sender.sendRichMessage("<red>Player not found!");
            return true;
        }

        int i;
        try {
            i = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendRichMessage("<red>Given disability id is not an integer!");
            return true;
        }

        if (i < 0 && i > DISABILITIES_TOTAL_AMOUNT-1) {
            sender.sendRichMessage("<red>Given disability id does not exist!");
            return true;
        }

        UUID uuid = player.getUniqueId();
        List<Disability> disabilities = plugin.disabilityMap.get(uuid);

        if (!disabilities.contains(Disability.get(i))) {
            plugin.disabilityMap.get(uuid).add(Disability.get(i));
            sender.sendRichMessage("<green>Successfully added disability to this player!");
        } else
            sender.sendRichMessage("<yellow>Player already has that disability!");

        return true;
    }

}
