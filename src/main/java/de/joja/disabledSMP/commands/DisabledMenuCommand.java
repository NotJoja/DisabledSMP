package de.joja.disabledSMP.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class DisabledMenuCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("<red>Only players can use that command!");
            return true;
        }

        Inventory disMenuInv = plugin.disManager.createMenu(player);
        player.openInventory(disMenuInv);

        return true;
    }

}
