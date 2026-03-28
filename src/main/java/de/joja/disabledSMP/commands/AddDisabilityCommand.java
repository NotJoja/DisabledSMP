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

        String playerName = args[0];
        Player player = plugin.getServer().getPlayer(playerName);
        if (player == null) {
            sender.sendRichMessage("<red>Player not found!");
            return true;
        }

        String disName = args[1];
        Disability dis = null;
        for (Disability d : Disability.values())
            if (d.enDataName.equals(disName) || d.deDataName.equals(disName))
                dis = d;

        if (dis == null) {
            sender.sendRichMessage("<red>Disability not found!");
            return true;
        }

        UUID uuid = player.getUniqueId();
        List<Disability> disabilities = plugin.disabilityMap.get(uuid);

        if (disabilities.contains(dis)) {
            sender.sendRichMessage("<yellow>Player already has that disability!");
        } else {
            disabilities.add(dis);
            dis.handler.addToPlayer(player);
            sender.sendRichMessage("<green>Successfully added disability to this player!");
        }

        return true;
    }

}
