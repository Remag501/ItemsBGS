package me.remag501.itemsbgs.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Interface for custom items that are thrown or require a target location.
 * Extends CustomItem.
 */
public interface ProjectileItem extends CustomItem {

    /**
     * Overrides the base CustomItem activation method with a default, empty implementation.
     * This method is provided to satisfy the requirement inherited from CustomItem
     * so that concrete classes (like MolotovItem) only have to focus on onThrow().
     * This uses the two-argument signature required by CustomItem.java.
     */
    @Override
    default void onActivate(Player activator, Plugin plugin) {
        // Implementation is deliberately empty. The ItemListener will call onThrow() instead.
    }

    /**
     * Calculates and returns the target location where the item effect should activate.
     * This is the "Calculation" phase performed by the listener.
     *
     * @param player The player who activated the item.
     * @return The calculated target Location, or null if no valid target was found within range.
     */
    Location getActivationLocation(Player player);

    /**
     * Defines the action that occurs when the item is thrown/used at a specific,
     * validated location. This is the "Execution" phase.
     *
     * @param player The player who activated the item.
     * @param targetLocation The validated target location.
     * @param plugin The main plugin instance for scheduling tasks.
     */
    void onThrow(Player player, Location targetLocation, Plugin plugin);
}
