package me.remag501.itemsbgs;

import me.remag501.itemsbgs.command.ItemsBGSCommand;
import me.remag501.itemsbgs.item.GrenadeItem;
import me.remag501.itemsbgs.item.MolotovItem;
import me.remag501.itemsbgs.item.TearGasItem;
import me.remag501.itemsbgs.listener.ItemListener;
import me.remag501.itemsbgs.manager.ItemManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the ItemsBGS Spigot plugin.
 * Now manages the ItemManager for a scalable item structure.
 */
public class ItemsBGS extends JavaPlugin {

    private static ItemsBGS plugin;
    private ItemManager itemManager;

    @Override
    public void onEnable() {
        plugin = this;
        getLogger().info("ItemsBGS: Initializing Item Manager and registering items.");

        // 1. Initialize and register items
        itemManager = new ItemManager(this);
        registerCustomItems();

        // 2. Register command executor (passing the manager)
        getCommand("itemsbgs").setExecutor(new ItemsBGSCommand(itemManager));

        // 3. Register the event listener (passing the manager)
        getServer().getPluginManager().registerEvents(new ItemListener(this, itemManager), this);

        getLogger().info("ItemsBGS has been enabled!");
    }

    /**
     * Registers all custom item classes with the ItemManager.
     * Adding a new item simply requires adding a line here.
     */
    private void registerCustomItems() {
        itemManager.registerItem(new MolotovItem());
        itemManager.registerItem(new GrenadeItem());
        itemManager.registerItem(new TearGasItem());
    }

    @Override
    public void onDisable() {
        getLogger().info("ItemsBGS has been disabled!");
    }

    /**
     * Static accessor for the plugin instance, used for NamespacedKey creation.
     */
    public static ItemsBGS getPlugin() {
        return plugin;
    }
}
