package de.joja.disabledSMP;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class FuckListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("you hurensohn");
        event.getPlayer().setHealth(20);
        ItemStack diaAxesStack = new ItemStack(Material.DIAMOND_AXE, 1);
        event.getPlayer().give(diaAxesStack);
    }

    @EventHandler
    public void onJoin(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("spring");
        Location loc = player.getLocation();
        loc.subtract(0, 1, 0);
        Block block = loc.getBlock();
        player.breakBlock(block);
    }

}
