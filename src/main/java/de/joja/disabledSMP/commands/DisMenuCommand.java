package de.joja.disabledSMP.commands;

import de.joja.disabledSMP.dismenu.ItemUtils;
import de.joja.disabledSMP.dismenu.menus.MainDisMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

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

        Inventory disMenuInv = new MainDisMenu(player.getUniqueId()).createMenuInv();
        player.openInventory(disMenuInv);

        return true;
    }

}
