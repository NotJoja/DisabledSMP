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

public class ListDisabilitiesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length < 1) {
            sender.sendRichMessage("<red>Not enough args!");
            return true;
        }

        if (args.length > 1) {
            sender.sendRichMessage("<red>Too many args!");
            return true;
        }

        Player player = plugin.getServer().getPlayer(args[0]);
        if (player == null) {
            sender.sendRichMessage("<red>Player not found!");
            return true;
        }

        UUID uuid = player.getUniqueId();
        sender.sendRichMessage("<gray>Disabilities of " + player.getName());
        List<Disability> disabilities = plugin.disabilityMap.get(uuid);
        for (Disability d : disabilities)
            sender.sendRichMessage("<gray>- " + d.ordinal());

        return true;
    }

}