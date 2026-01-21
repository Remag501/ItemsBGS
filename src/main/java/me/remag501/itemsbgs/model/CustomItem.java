package me.remag501.itemsbgs.model;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public interface CustomItem {

    /**
     * @return A unique String identifier for the item (e.g., "molotov", "grenade").
     */
    String getId();

    /**
     * @return The item stack used for creation and inventory display.
     */
    ItemStack getItem(int amount);

    /**
     * The location-agnostic logic that runs when the item is activated.
     * This method is primarily intended for non-projectile utility items (e.g., Riot Shield).
     * Projectile items should use onThrow().
     * @param activator The player who activated the item.
     * @param plugin The main plugin instance for scheduling tasks.
     */
    void onActivate(Player activator, Plugin plugin);
}
