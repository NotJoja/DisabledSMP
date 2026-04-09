package de.joja.disabledSMP.commands;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.menus.MainDisMenu;
import de.joja.disabledSMP.utils.DConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class DisCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (args.length == 0) {
            sender.sendRichMessage("<red>Usage: /dis <add|rm|menu|list> [args]");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "add" -> handleAdd(sender, args);
            case "rm" -> handleRemove(sender, args);
            case "menu" -> handleMenu(sender);
            case "list" -> handleList(sender, args);
            default -> sender.sendRichMessage("<red>Unknown subcommand!");
        }

        return true;
    }

    private Player getTargetPlayer(CommandSender sender, String[] args, int index) {
        if (args.length > index) {
            Player p = Bukkit.getPlayer(args[index]);
            if (p == null) {
                sender.sendRichMessage("<red>Player not found!");
                return null;
            }
            return p;
        } else if (sender instanceof Player player) {
            return player;
        } else {
            sender.sendRichMessage("<red>You must specify a player!");
            return null;
        }
    }

    private Disability getDisability(CommandSender sender, String name) {
        for (Disability d : Disability.values()) {
            if (d.enDataName.equalsIgnoreCase(name) || d.deDataName.equalsIgnoreCase(name))
                return d;
        }
        sender.sendRichMessage("<red>Disability not found!");
        return null;
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendRichMessage("<red>Usage: /dis add <disability> [player]");
            return;
        }

        Disability dis = getDisability(sender, args[1]);
        if (dis == null) return;

        Player target = getTargetPlayer(sender, args, 2);
        if (target == null) return;

        UUID uuid = target.getUniqueId();
        List<Disability> disabilities = plugin.disabilityMap.getOrDefault(uuid, new ArrayList<>());
        plugin.disabilityMap.putIfAbsent(uuid, disabilities);

        if (disabilities.contains(dis)) {
            sender.sendRichMessage("<yellow>Player already has that disability!");
        } else {
            disabilities.add(dis);
            dis.handler.addToPlayer(target);
            sender.sendRichMessage("<green>Successfully added disability to " + target.getName() + "!");
        }
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendRichMessage("<red>Usage: /dis rm <disability> [player]");
            return;
        }

        Disability dis = getDisability(sender, args[1]);
        if (dis == null) return;

        Player target = getTargetPlayer(sender, args, 2);
        if (target == null) return;

        UUID uuid = target.getUniqueId();
        List<Disability> disabilities = plugin.disabilityMap.get(uuid);

        if (disabilities == null || !disabilities.contains(dis)) {
            sender.sendRichMessage("<yellow>Player does not have that disability!");
            return;
        }

        disabilities.remove(dis);
        dis.handler.removeFromPlayer(target);
        sender.sendRichMessage("<green>Successfully removed disability from " + target.getName() + "!");
    }

    private void handleMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendRichMessage("<red>Only players can use this command!");
            return;
        }
        Inventory menu = new MainDisMenu(player.getUniqueId()).createMenuInv();
        player.openInventory(menu);
    }

    private void handleList(CommandSender sender, String[] args) {
        Player target = getTargetPlayer(sender, args, 1);
        if (target == null) return;

        List<Disability> disabilities = plugin.disabilityMap.getOrDefault(target.getUniqueId(), new ArrayList<>());

        sender.sendRichMessage("<gray>Disabilities of " + target.getName() + ":");
        if (disabilities.isEmpty()) {
            sender.sendRichMessage("<yellow>None");
            return;
        }

        for (Disability d : disabilities) {
            sender.sendRichMessage("<gray>- " + (DConfig.LANGUAGE.equals("en") ? d.enName : d.deName));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String s : new String[]{"add", "rm", "menu", "list"})
                if (s.startsWith(args[0].toLowerCase())) completions.add(s);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("rm")) {
                for (Disability d : Disability.values()) {
                    if (d.enDataName.toLowerCase().startsWith(args[1].toLowerCase()))
                        completions.add(d.enDataName);
                    if (d.deDataName.toLowerCase().startsWith(args[1].toLowerCase()))
                        completions.add(d.deDataName);
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        completions.add(p.getName());
                }
            }
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("rm"))) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                    completions.add(p.getName());
            }
        }

        return completions;
    }
}