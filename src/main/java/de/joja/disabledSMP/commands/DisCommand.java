package de.joja.disabledSMP.commands;

import de.joja.disabledSMP.advancements.AdvancementManager;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.menus.MainDisMenu;
import de.joja.disabledSMP.smoking.SmokingManager;
import de.joja.disabledSMP.utils.DConfig;
import de.joja.disabledSMP.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
            String subs = "add|rm|menu|list" + (isAdmin(sender) ? "|admin|reload" : "");
            msg(sender, "<red>Usage: /dis <" + subs + "> [args]",
                    "<red>Verwendung: /dis <" + subs + "> [args]");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "add" -> handleAdd(sender, args);
            case "rm" -> handleRemove(sender, args);
            case "menu" -> handleMenu(sender, args);
            case "list" -> handleList(sender, args);
            case "admin" -> handleAdmin(sender, args);
            case "reload" -> handleReload(sender);
            default -> msg(sender, "<red>Unknown subcommand!",
                "<red>Unbekannter Unterbefehl!");
        }

        return true;
    }

    /** Sends a message in the sender's language. */
    private static void msg(CommandSender sender, String en, String de) {
        sender.sendRichMessage(Lang.of(sender).pick(en, de));
    }

    private boolean isAdmin(CommandSender sender) {
        return sender.hasPermission("disabledSMP.disability_admin") || sender.isOp();
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (!isAdmin(sender)) {
            msg(sender, "<red>You don't have permission to use this command!",
                "<red>Du hast keine Berechtigung für diesen Befehl!");
            return;
        }

        if (args.length < 2) {
            msg(sender, "<red>Usage: /dis add <disability|everything> [player]",
                "<red>Verwendung: /dis add <disability|alles> [player]");
            return;
        }

        String disName = args[1];
        boolean all = isEverything(disName);
        Disability dis = null;
        for (Disability d : Disability.values())
            if (d.enDataName.equalsIgnoreCase(disName) || d.deDataName.equalsIgnoreCase(disName))
                dis = d;

        if (dis == null && !all) {
            msg(sender, "<red>Disability not found!",
                "<red>Disability nicht gefunden!");
            return;
        }

        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                msg(sender, "<red>Player not found!",
                "<red>Spieler nicht gefunden!");
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                msg(sender, "<red>You must specify a player!",
                "<red>Du musst einen Spieler angeben!");
                return;
            }
            target = (Player) sender;
        }

        UUID uuid = target.getUniqueId();
        List<Disability> disabilities = plugin.disabilityMap.get(uuid);
        if (disabilities == null) {
            disabilities = new ArrayList<>();
            plugin.disabilityMap.put(uuid, disabilities);
        }

        if (all) {
            int added = 0;
            for (Disability d : Disability.values())
                if (!disabilities.contains(d)) {
                    disabilities.add(d);
                    d.handler.addToPlayer(target);
                    added++;
                }
            msg(sender, "<green>Added " + added + " disabilities to " + target.getName() + "!",
                "<green>" + added + " Disabilities zu " + target.getName() + " hinzugefügt!");
            return;
        }

        if (disabilities.contains(dis)) {
            msg(sender, "<yellow>Player already has that disability!",
                "<yellow>Der Spieler hat diese Disability bereits!");
        } else {
            disabilities.add(dis);
            dis.handler.addToPlayer(target);
            msg(sender, "<green>Successfully added disability to " + target.getName() + "!",
                "<green>Disability erfolgreich zu " + target.getName() + " hinzugefügt!");
        }
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (!isAdmin(sender)) {
            msg(sender, "<red>You don't have permission to use this command!",
                "<red>Du hast keine Berechtigung für diesen Befehl!");
            return;
        }

        if (args.length < 2) {
            msg(sender, "<red>Usage: /dis rm <disability|everything> [player]",
                "<red>Verwendung: /dis rm <disability|alles> [player]");
            return;
        }

        String disName = args[1];
        boolean all = isEverything(disName);
        Disability dis = null;
        for (Disability d : Disability.values())
            if (d.enDataName.equalsIgnoreCase(disName) || d.deDataName.equalsIgnoreCase(disName))
                dis = d;

        if (dis == null && !all) {
            msg(sender, "<red>Disability not found!",
                "<red>Disability nicht gefunden!");
            return;
        }

        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                msg(sender, "<red>Player not found!",
                "<red>Spieler nicht gefunden!");
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                msg(sender, "<red>You must specify a player!",
                "<red>Du musst einen Spieler angeben!");
                return;
            }
            target = (Player) sender;
        }

        UUID uuid = target.getUniqueId();
        List<Disability> disabilities = plugin.disabilityMap.get(uuid);
        if (all) {
            if (disabilities == null || disabilities.isEmpty()) {
                msg(sender, "<yellow>Player has no disabilities!",
                    "<yellow>Der Spieler hat keine Disabilities!");
                return;
            }
            int removed = disabilities.size();
            for (Disability d : new ArrayList<>(disabilities))
                d.handler.removeFromPlayer(target);
            disabilities.clear();
            msg(sender, "<green>Removed " + removed + " disabilities from " + target.getName() + "!",
                "<green>" + removed + " Disabilities von " + target.getName() + " entfernt!");
            return;
        }

        if (disabilities == null || !disabilities.contains(dis)) {
            msg(sender, "<yellow>Player does not have that disability!",
                "<yellow>Der Spieler hat diese Disability nicht!");
            return;
        }

        disabilities.remove(dis);
        dis.handler.removeFromPlayer(target);
        msg(sender, "<green>Successfully removed disability from " + target.getName() + "!",
                "<green>Disability erfolgreich von " + target.getName() + " entfernt!");
    }

    /** "everything" (or "alles") instead of a disability name means all of them. */
    private static boolean isEverything(String name) {
        return name.equalsIgnoreCase("everything") || name.equalsIgnoreCase("alles");
    }

    /** Reads config.yml again. Values that are used while playing apply right away. */
    private void handleReload(CommandSender sender) {
        if (!isAdmin(sender)) {
            msg(sender, "<red>You don't have permission to use this command!",
                "<red>Du hast keine Berechtigung für diesen Befehl!");
            return;
        }

        DConfig.reload();

        // attribute based disabilities only apply their values when they are added: apply them again
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<Disability> disabilities = plugin.disabilityMap.get(player.getUniqueId());
            if (disabilities == null)
                continue;
            if (disabilities.contains(Disability.OVERWEIGHT))
                Disability.OVERWEIGHT.handler.addToPlayer(player);
            if (disabilities.contains(Disability.DWARFISM))
                Disability.DWARFISM.handler.addToPlayer(player);
        }

        msg(sender, "<green>Config reloaded! (Resource pack settings apply the next time a player joins.)",
                "<green>Config neu geladen! (Resource-Pack-Einstellungen gelten beim nächsten Beitritt.)");
    }

    private void handleAdmin(CommandSender sender, String[] args) {
        if (!isAdmin(sender)) {
            msg(sender, "<red>You don't have permission to use this command!",
                "<red>Du hast keine Berechtigung für diesen Befehl!");
            return;
        }

        if (args.length < 2) {
            msg(sender, "<red>Usage: /dis admin <wheelchair|nails|cigarette|vape> [player]",
                "<red>Verwendung: /dis admin <wheelchair|nails|cigarette|vape> [player]");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "wheelchair" -> handleAdminGive(sender, args, plugin.wheelchairManager.createItem(),
                    "a wheelchair", "einen Rollstuhl");
            case "nails" -> handleAdminGive(sender, args, plugin.nailsManager.createItem(),
                    "nails", "Nägel");
            case "cigarette" -> handleAdminGive(sender, args,
                    plugin.smokingManager.createItem(SmokingManager.Type.CIGARETTE, SmokingManager.Type.CIGARETTE.maxUses),
                    "a cigarette", "eine Zigarette");
            case "vape" -> handleAdminGive(sender, args,
                    plugin.smokingManager.createItem(SmokingManager.Type.VAPE, SmokingManager.Type.VAPE.maxUses),
                    "a vape", "eine Vape");
            default -> msg(sender, "<red>Unknown admin subcommand!",
                "<red>Unbekannter Admin-Unterbefehl!");
        }
    }

    /** Gives the item to the named player (or the sender). enItem / deItem is how it's called in the message. */
    private void handleAdminGive(CommandSender sender, String[] args, ItemStack item, String enItem, String deItem) {
        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                msg(sender, "<red>Player not found!",
                "<red>Spieler nicht gefunden!");
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                msg(sender, "<red>You must specify a player!",
                "<red>Du musst einen Spieler angeben!");
                return;
            }
            target = (Player) sender;
        }

        target.getInventory().addItem(item).values()
                .forEach(left -> target.getWorld().dropItemNaturally(target.getLocation(), left));
        msg(sender, "<green>Gave " + enItem + " to " + target.getName() + "!",
                "<green>" + target.getName() + " hat " + deItem + " bekommen!");
    }

    private void handleMenu(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            msg(sender, "<red>Only players can use this command!",
                "<red>Nur Spieler können diesen Befehl benutzen!");
            return;
        }

        if (args.length != 1) {
            msg(sender, "<red>No arguments needed for this command!",
                "<red>Dieser Befehl braucht keine Argumente!");
            return;
        }

        Inventory disMenuInv = new MainDisMenu(player.getUniqueId()).createMenuInv();
        player.openInventory(disMenuInv);
        plugin.advancementManager.grant(player, AdvancementManager.GETTING_TO_KNOW_YOURSELF);
    }

    private void handleList(CommandSender sender, String[] args) {
        if (!isAdmin(sender)) {
            msg(sender, "<red>You don't have permission to use this command!",
                "<red>Du hast keine Berechtigung für diesen Befehl!");
            return;
        }

        Player target;

        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                msg(sender, "<red>Player not found!",
                "<red>Spieler nicht gefunden!");
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                msg(sender, "<red>You must specify a player!",
                "<red>Du musst einen Spieler angeben!");
                return;
            }
            target = (Player) sender;
        }

        UUID uuid = target.getUniqueId();
        List<Disability> disabilities = plugin.disabilityMap.get(uuid);

        msg(sender, "<gray>Disabilities of " + target.getName() + ":",
                "<gray>Disabilities von " + target.getName() + ":");

        if (disabilities == null || disabilities.isEmpty()) {
            msg(sender, "<yellow>None",
                "<yellow>Keine");
            return;
        }

        for (Disability d : disabilities) {
            sender.sendRichMessage("<gray>- " + Lang.of(sender).pick(d.enName, d.deName));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String s : new String[]{"add", "rm", "menu", "list", "admin", "reload"}) {
                if ((s.equals("admin") || s.equals("reload")) && !isAdmin(sender)) continue;
                if (s.startsWith(args[0].toLowerCase())) completions.add(s);
            }
        } else if (args.length == 2) {
            if ((args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("rm")) && isAdmin(sender)) {
                if ("everything".startsWith(args[1].toLowerCase()))
                    completions.add("everything");
                for (Disability d : Disability.values()) {
                    if (d.enDataName.toLowerCase().startsWith(args[1].toLowerCase()))
                        completions.add(d.enDataName);
                    else if (d.deDataName.toLowerCase().startsWith(args[1].toLowerCase()))
                        completions.add(d.deDataName);
                }
            } else if (args[0].equalsIgnoreCase("admin") && isAdmin(sender)) {
                for (String item : new String[]{"wheelchair", "nails", "cigarette", "vape"})
                    if (item.startsWith(args[1].toLowerCase()))
                        completions.add(item);
            } else if (args[0].equalsIgnoreCase("list") && isAdmin(sender)) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        completions.add(p.getName());
                }
            }
        } else if (args.length == 3) {
            if (((args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("rm"))
                    || args[0].equalsIgnoreCase("admin")) && isAdmin(sender)) {
                for (Player p : Bukkit.getOnlinePlayers())
                    if (p.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        completions.add(p.getName());
            }
        }

        return completions;
    }
}