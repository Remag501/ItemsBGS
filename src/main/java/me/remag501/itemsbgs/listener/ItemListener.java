package me.remag501.itemsbgs.listener;

import me.remag501.itemsbgs.item.CustomItem;
import me.remag501.itemsbgs.manager.ItemManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles custom item activation when a player drops an item.
 */
public class ItemListener implements Listener {

    private final Plugin plugin;
    private final ItemManager itemManager;

    public ItemListener(Plugin plugin, ItemManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        String itemId = itemManager.getCustomItemId(droppedItem);

        if (itemId != null) {
            // Stop the item entity from spawning and revert inventory change (for control/consumption)
            event.setCancelled(true);

            CustomItem customItem = itemManager.getItemById(itemId);
            if (customItem == null) {
                event.getPlayer().sendMessage("§cError: Custom item logic not found for " + itemId);
                return;
            }

            Player player = event.getPlayer();

            // Item consumption and entity manipulation:
            // This line prevents the item entity from spawning and, based on your findings,
            // correctly handles the consumption of the item from the inventory.
            event.getItemDrop().setItemStack(new ItemStack(Material.AIR));

            // Force client update to resolve single-item stack visual bugs
            player.updateInventory();

            // Calculate activation location (simulating a throw)
            Location activationLoc = player.getTargetBlock(null, 10).getLocation().add(0.5, 0.5, 0.5);

            // Execute the activation logic after a short delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    customItem.onActivate(player, activationLoc, plugin);
                }
            }.runTaskLater(plugin, 5L); // 5 ticks delay
        }
    }
}
