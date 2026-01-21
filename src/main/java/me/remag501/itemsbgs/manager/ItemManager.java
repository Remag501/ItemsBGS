package me.remag501.itemsbgs.manager;

import me.remag501.itemsbgs.ItemsBGS;
import me.remag501.itemsbgs.model.CustomItem;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Centralized manager for registering, retrieving, and identifying custom items.
 */
public class ItemManager {

    // Key used in PersistentDataContainer to store the item's unique ID
    private final NamespacedKey itemKey;
    private final Map<String, CustomItem> registeredItems = new HashMap<>();

    public ItemManager(ItemsBGS plugin) {
        this.itemKey = new NamespacedKey(plugin, "custom_item_id");
    }

    /**
     * Registers a custom item instance. This should be done during plugin startup.
     * @param item The CustomItem implementation to register.
     */
    public void registerItem(CustomItem item) {
        registeredItems.put(item.getId(), item);
        ItemsBGS.getPlugin().getLogger().info("Registered custom item: " + item.getId());
    }

    /**
     * Gets a registered CustomItem by its ID.
     * @param id The unique identifier string.
     * @return The CustomItem object, or null if not found.
     */
    public CustomItem getItemById(String id) {
        return registeredItems.get(id);
    }

    /**
     * Gets the unique ID of the custom item held in an ItemStack.
     * @param item The ItemStack to check.
     * @return The item's unique ID, or null if it's not a custom item.
     */
    public String getCustomItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
    }

    /**
     * Creates a new instance of the custom item with the given amount.
     * @param id The ID of the item to create.
     * @param amount The quantity.
     * @return The ItemStack, or null if the ID is invalid.
     */
    public ItemStack createItemStack(String id, int amount) {
        CustomItem customItem = getItemById(id);
        if (customItem != null) {
            ItemStack stack = customItem.getItem(amount);
            // Apply the unique ID to the PersistentDataContainer
            ItemMeta meta = stack.getItemMeta();
            meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, id);
            stack.setItemMeta(meta);
            return stack;
        }
        return null;
    }

    /**
     * @return A set of all registered item IDs.
     */
    public Set<String> getRegisteredIds() {
        return registeredItems.keySet();
    }
}
