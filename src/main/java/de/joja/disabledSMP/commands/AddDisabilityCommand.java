package de.joja.disabledSMP.commands;

import de.joja.disabledSMP.Disability;
import de.joja.disabledSMP.DisabledSMP;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.Disability.DISABILITIES_TOTAL_AMOUNT;

public class AddDisabilityCommand implements CommandExecutor {

    DisabledSMP plugin;

    public AddDisabilityCommand(DisabledSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

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
        List<Disability> disabilities = plugin.disabilityM.disabilityMap.get(uuid);

        if (!disabilities.contains(Disability.get(i))) {
            plugin.disabilityM.disabilityMap.get(uuid).add(Disability.get(i));
            sender.sendRichMessage("<green>Successfully added disability to this player!");
        } else
            sender.sendRichMessage("<yellow>Player already has that disability!");

        return true;
    }

}
