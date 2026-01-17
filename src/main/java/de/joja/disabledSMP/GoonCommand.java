package de.joja.disabledSMP;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GoonCommand implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("You can't goon, you aren't a player!");
            return true;
        }

        final Player player = (Player) sender;

        ItemStack goonBucket = new ItemStack(Material.MILK_BUCKET, 1);

        player.give(goonBucket);

        return true;
    }

}
