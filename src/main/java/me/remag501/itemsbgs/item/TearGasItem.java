package me.remag501.itemsbgs.item;

import me.remag501.itemsbgs.runnable.TearGasTracker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound; // Added for throw sound
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Arrays;

/**
 * Concrete implementation of the Tear Gas item.
 * Throws a custom Item entity that spawns an AreaEffectCloud on impact.
 */
public class TearGasItem extends AbstractTargetingItem {

    private static final String ID = "teargas";

    // Item projectile constants
    public static final String METADATA_KEY = "TEARGAS_PROJECTILE"; // Unique key for this projectile
    public static final int ACTIVATION_TICKS_THRESHOLD = 10; // Min ticks lived before stationary check
    public static final int PROC_DELAY_TICKS = 2; // Ticks to wait after landing while stationary
    public static final double STATIONARY_VELOCITY_THRESHOLD = 0.005; // Velocity threshold for 'stationary'
    public static final int FAILSAFE_DESPAWN_TICKS = 60; // Max flight time before forced activation/despawn (3 seconds)

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ItemStack getItem(int amount) {
        ItemStack item = new ItemStack(Material.GLASS_BOTTLE, amount); // Changed to Glass Bottle for visual
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§8§lTear Gas §8Canister §c✪✪");
        meta.setLore(Arrays.asList(
                "§8• §fA non-lethal projectile that",
                "§fcauses temporary blindness",
                "§fand disorientation in an area.",
                "§r",
                "§7§o(( Right-click to use. ))"
        ));

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Executes the Tear Gas throw logic.
     * Launches a custom Item entity that is tracked by TearGasTracker.
     */
    @Override
    public void onThrow(Player activator, Location targetLocation, Plugin plugin) {
        activator.sendMessage("§a§l(!) §aTear Gas launched!");
        activator.getWorld().playSound(activator.getLocation(), Sound.ENTITY_EGG_THROW, 0.5f, 1.0f); // Throw sound

        Location eyeLoc = activator.getEyeLocation();

        // 1. Create the item to be "thrown"
        ItemStack canisterStack = new ItemStack(Material.GLASS_BOTTLE); // Visual item

        // 2. Spawn the Item entity
        Item tearGasCanister = activator.getWorld().dropItem(eyeLoc, canisterStack);

        // --- Essential Configuration for a "Projectile" Item ---
        tearGasCanister.setInvulnerable(true);
        tearGasCanister.setPickupDelay(32767); // Max pickup delay
        tearGasCanister.setGravity(true); // Ensure it has gravity

        // 3. Apply the velocity
        Vector velocity = targetLocation.toVector().subtract(eyeLoc.toVector());
        velocity.normalize().multiply(0.9);
        velocity.setY(velocity.getY() + 0.1); // Arc
        tearGasCanister.setVelocity(velocity);

        // 4. TAG THE ITEM with metadata
        tearGasCanister.setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, true));

        // 5. START THE NEW TRACKING TASK
        // The TearGasTracker will spawn the AreaEffectCloud when the item lands.
        new TearGasTracker(tearGasCanister, plugin).runTaskTimer(plugin, 1L, 1L);
    }
}