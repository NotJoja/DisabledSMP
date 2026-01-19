package de.joja.disabledSMP.commands;

import de.joja.disabledSMP.disablities.menu.DisMenuType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class DisMenuCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("<red>Only players can use that command!");
            return true;
        }

        if (args.length != 0) {
            sender.sendMessage("<red>No args for this command!");
            return true;
        }

        Inventory disMenuInv = plugin.disManager.createDisabilitiesMenu(player, "Disabilities of " + player.getName(), DisMenuType.SHOW_MINE);
        player.openInventory(disMenuInv);

        return true;
    }

}
