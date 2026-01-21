package me.remag501.itemsbgs.item;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

/**
 * Provides a base implementation for ProjectileItem.
 * All items extending this class will automatically get a default 20-block
 * ray-trace targeting system. This removes redundant targeting code from child classes.
 */
public abstract class AbstractTargetingItem implements ProjectileItem {

    // Define the common default range here
    protected int defaultRange;

    protected AbstractTargetingItem() {
        this.defaultRange = 50;
    }

    protected AbstractTargetingItem(int defaultRange) {
        this.defaultRange = defaultRange;
    }

    /**
     * Implements the preferred calculation: a simple ray-trace to find the targeted block.
     * This method is automatically inherited by all items extending this class.
     */

    @Override
    public Location getActivationLocation(Player player) {
        // Find the block the player is looking at within the default range.
        // The second argument in getTargetBlock is maxDistance (range)
        Block targetBlock = player.getTargetBlock(null, defaultRange);

        defaultRange = 50;

        if (targetBlock == null) {
            // Return null if no valid solid block is targeted
            return null;
        }

        // Return the center location of the targeted block
        return targetBlock.getLocation().add(0.5, 0.5, 0.5);
    }

    // --- Abstract methods required by CustomItem/ProjectileItem ---

    @Override
    public abstract String getId();

    @Override
    public abstract ItemStack getItem(int amount);

    /**
     * Abstract method required for the projectile execution phase.
     */
    @Override
    public abstract void onThrow(Player player, Location targetLocation, Plugin plugin);
}
