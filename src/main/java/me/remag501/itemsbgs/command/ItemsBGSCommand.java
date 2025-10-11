package me.remag501.itemsbgs.command;

import me.remag501.itemsbgs.manager.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the main /itemsbgs command and its subcommands (like 'give').
 */
public class ItemsBGSCommand implements CommandExecutor, TabCompleter {

    private final ItemManager itemManager;

    public ItemsBGSCommand(ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§a--- ItemsBGS Command Help ---");
        sender.sendMessage("§e/itemsbgs give <player> <item_id> [amount]");
        sender.sendMessage("§7Example: /itemsbgs give Remag501 grenade 5");
        sender.sendMessage("§7Available items: " + String.join(", ", itemManager.getRegisteredIds()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("give")) {
            return handleGiveCommand(sender, args);
        } else {
            sender.sendMessage("§cUnknown subcommand. Use /itemsbgs help or /itemsbgs give.");
            return true;
        }
    }

    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("itemsbgs.give")) {
            sender.sendMessage("§cYou do not have permission to use the 'give' command.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /itemsbgs give <player> <item_id> [amount]");
            return true;
        }

        // Parse player, item, and amount
        Player targetPlayer = Bukkit.getPlayer(args[1]);
        String itemId = args[2].toLowerCase();
        int amount = 1;

        if (targetPlayer == null) {
            sender.sendMessage("§cPlayer '" + args[1] + "' not found or offline.");
            return true;
        }

        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                amount = Math.max(1, Math.min(64, amount)); // Clamp amount
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid amount specified. Defaulting to 1.");
            }
        }

        // Create and give the item
        ItemStack itemToGive = itemManager.createItemStack(itemId, amount);

        if (itemToGive != null) {
            targetPlayer.getInventory().addItem(itemToGive);
            targetPlayer.sendMessage("§aYou received §b" + amount + " " + itemId + "§a from " + sender.getName() + ".");

            if (!sender.equals(targetPlayer)) {
                sender.sendMessage("§aSuccessfully gave §b" + amount + " " + itemId + "§a to " + targetPlayer.getName() + ".");
            }
        } else {
            sender.sendMessage("§cInvalid item ID: " + itemId + ". Use one of: " + String.join(", ", itemManager.getRegisteredIds()));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            // Suggest subcommands
            return List.of("give", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args[0].equalsIgnoreCase("give") && sender.hasPermission("itemsbgs.give")) {
            if (args.length == 2) {
                // Suggest online players
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
            if (args.length == 3) {
                // Suggest item IDs
                String partialId = args[2].toLowerCase();
                return itemManager.getRegisteredIds().stream()
                        .filter(id -> id.startsWith(partialId))
                        .collect(Collectors.toList());
            }
            if (args.length == 4) {
                // Suggest quantity
                return List.of("1", "5", "16", "64");
            }
        }

        return new ArrayList<>();
    }
}
