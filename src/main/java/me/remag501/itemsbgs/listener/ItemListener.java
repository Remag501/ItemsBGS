package me.remag501.itemsbgs.listener;

import me.remag501.itemsbgs.ItemsBGS;
import me.remag501.itemsbgs.model.CustomItem;
import me.remag501.itemsbgs.model.ProjectileItem;
import me.remag501.itemsbgs.manager.ItemManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.Optional;

/**
 * Handles all player interactions (clicks) for custom item activation.
 * This listener acts as the gatekeeper, deciding whether to activate the item
 * and managing the item consumption process.
 */
public class ItemListener implements Listener {

    private final ItemsBGS plugin;
    private final ItemManager itemManager;

    public ItemListener(ItemsBGS plugin, ItemManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 1. Filter for a right-click action with an item in hand
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack heldItem = event.getItem();
        if (heldItem == null || heldItem.getType() == Material.AIR) {
            return;
        }

        Player player = event.getPlayer();

        // 2. Identify if the item is a custom item (Correctly chaining ItemManager methods)
        String customItemId = itemManager.getCustomItemId(heldItem);
        if (customItemId == null) {
            return;
        }

        Optional<CustomItem> customItemOpt = Optional.ofNullable(itemManager.getItemById(customItemId));
        if (customItemOpt.isEmpty()) {
            return;
        }

        CustomItem customItem = customItemOpt.get();

        // Prevent block interaction (like opening doors) when right-clicking with a custom item
        event.setCancelled(true);

        // 3. Handle Projectile/Targeting Items
        if (customItem instanceof ProjectileItem projectileItem) {

            // Phase 1: Calculation (Delegate targeting logic to the item)
            Location activationLoc = projectileItem.getActivationLocation(player);

            // Phase 2: Validation (Listener acts as the gatekeeper)
            if (activationLoc == null) {
                player.sendMessage("§cNo valid target found within range!");
                return;
            }

            // Phase 3: Consumption & Execution (Only if target is valid)
            ItemStack oneUnitToConsume = heldItem.clone();
            oneUnitToConsume.setAmount(1);

            // This is the reliable, client-syncing way to consume one item
            player.getInventory().removeItem(oneUnitToConsume);
            player.updateInventory();

            projectileItem.onThrow(player, activationLoc, plugin);

        } else {
            // 4. Handle Location-Agnostic/Utility Items

            // Utility items don't consume on activation by default; logic is in onActivate()
            customItem.onActivate(player, plugin);
        }
    }
}
