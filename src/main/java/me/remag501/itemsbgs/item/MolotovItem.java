package me.remag501.itemsbgs.item;

import me.remag501.itemsbgs.ItemsBGS;
import me.remag501.itemsbgs.model.AbstractTargetingItem;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;

/**
 * Concrete implementation of the Molotov Cocktail item.
 */
public class MolotovItem extends AbstractTargetingItem {

    private static final String ID = "molotov";
    private static final int FIRE_RADIUS = 2; // For a 5x5 area
    private static final int FIRE_DURATION_TICKS = 3 * 20; // 3 seconds

    // NEW LOGIC CONSTANT: Minimum ticks lived before we consider the item "landed"
    // This value is based on typical projectile flight time.
    private static final int ACTIVATION_TICKS_THRESHOLD = 10; // 1 second flight minimum

    // NEW LOGIC CONSTANT: Ticks to wait after landing before final proc
    private static final int PROC_DELAY_TICKS = 2;

    // Define the unique key for the metadata tag
    public static final String METADATA_KEY = "MOLOTOV_PROJECTILE";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ItemStack getItem(int amount) {
        ItemStack item = new ItemStack(Material.REDSTONE_TORCH, amount);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§6§lMolotov §6Cocktail §e✪✪✪");
        meta.setLore(Arrays.asList(
                "§8• §fA crude explosive that causes",
                "§fa short-lived patch of fire.",
                "§r",
                "§7§o(( Right-click to use. ))"
        ));

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Executes the Molotov effect logic after consumption and target validation.
     */
    @Override
    public void onThrow(Player activator, Location targetLocation, Plugin plugin) {
        activator.sendMessage("§a§l(!) §aMolotov thrown!");

        Location eyeLoc = activator.getEyeLocation();

        // 1. Create the item to be "thrown"
        ItemStack torchStack = new ItemStack(Material.SPLASH_POTION);
        // Change color
        PotionMeta meta = (PotionMeta) torchStack.getItemMeta();
        if (meta != null) {
            meta.setColor(Color.ORANGE);
            torchStack.setItemMeta(meta);
        }

        // 2. Spawn the Item entity
        Item molotovItem = activator.getWorld().dropItem(eyeLoc, torchStack);

        // --- Essential Configuration for a "Projectile" Item ---
        molotovItem.setInvulnerable(true);
        molotovItem.setPickupDelay(32767); // Max pickup delay

        // 3. Apply the velocity
        Vector velocity = targetLocation.toVector().subtract(eyeLoc.toVector());
        velocity.normalize().multiply(1.5);
        velocity.setY(velocity.getY() + 0.3);
        molotovItem.setVelocity(velocity);

        // 4. TAG THE ITEM with metadata
        molotovItem.setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, true));

        // 5. START THE NEW TRACKING TASK
        new MolotovTracker(molotovItem).runTaskTimer(plugin, 1L, 1L);
    }

    /**
     * The core logic for the Molotov's effect.
     */
    private void activateMolotov(Item molotovItem) {
        Location location = molotovItem.getLocation();

        // 1. Play the explosion/shatter effect
        location.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);

        // 2. Create the fire effect
        for (int x = -FIRE_RADIUS; x <= FIRE_RADIUS; x++) {
            for (int z = -FIRE_RADIUS; z <= FIRE_RADIUS; z++) {
                Block blockBelow = location.clone().add(x, -1, z).getBlock();
                Block fireBlock = blockBelow.getRelative(0, 1, 0);

                // Check if the block below is solid and the block for fire is air
                if (blockBelow.getType().isSolid() && fireBlock.getType() == Material.AIR) {
                    fireBlock.setType(Material.FIRE);

                    // Schedule the fire to be extinguished
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (fireBlock.getType() == Material.FIRE) {
                                fireBlock.setType(Material.AIR);
                            }
                        }
                    }.runTaskLater(ItemsBGS.getPlugin(), FIRE_DURATION_TICKS);
                }
            }
        }

        // 3. Remove the item entity
        molotovItem.remove();
    }

    /**
     * A repeating task to track the state of the Molotov Item entity.
     * It uses a combination of low velocity and age to ensure the projectile
     * has settled and stopped moving before activating.
     */
    private class MolotovTracker extends BukkitRunnable {

        private final Item item;
        private int ticksStationary = 0;

        public MolotovTracker(Item item) {
            this.item = item;
        }

        @Override
        public void run() {
            // Check if the item is no longer valid
            if (!item.isValid()) {
                this.cancel();
                return;
            }

            // Primary check: Has the item lived long enough to complete its flight arc?
            if (item.getTicksLived() < ACTIVATION_TICKS_THRESHOLD) {
                return;
            }

            // Secondary check: Is the item effectively stationary?
            // This is the check that was problematic, but is now more reliable due to the age gate.
            // Check for near-zero velocity. 0.005 is a standard low-velocity threshold.
            if (!item.getLocation().subtract(0,1,0).getBlock().getType().isAir()) {
                ticksStationary++;
            } else {
                // If it's moving fast, reset the counter
                ticksStationary = 0;
            }

            // Final check: Has it been stationary for the required delay?
            if (ticksStationary >= PROC_DELAY_TICKS) {
                // To prevent activation while bouncing, we add one last check for low velocity
                // and if it's currently on a solid block (or water, which also stops item movement)
                if (item.isOnGround() || item.isInWater()) {
                    activateMolotov(item);
                    this.cancel(); // Stop the task after activation
                }
            }

            // FAILSAFE: Remove the item if it lives too long (e.g., gets stuck in a wall)
            // Default despawn is 6000 ticks (5 minutes), but we can make this shorter.
            if (item.getTicksLived() > 60) { // e.g., 3 seconds stuck in a weird spot
                activateMolotov(item); // Proc it anyway or just remove it
                this.cancel();
            }
        }
    }
}