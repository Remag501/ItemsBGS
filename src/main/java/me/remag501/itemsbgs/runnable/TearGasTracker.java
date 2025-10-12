package me.remag501.itemsbgs.runnable; // Same package as TearGasItem

import me.remag501.itemsbgs.item.TearGasItem;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material; // For block checks
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block; // For block checks
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * A repeating task to track the thrown Tear Gas Item entity.
 * Spawns an AreaEffectCloud when the item lands and settles.
 */
public class TearGasTracker extends BukkitRunnable {

    private final Item item;
    private final Plugin plugin;
    private int ticksStationary = 0;

    // AreaEffectCloud Constants (Moved from TearGasItem for encapsulation)
    private static final int CLOUD_DURATION_TICKS = 10 * 20; // 10 seconds total duration
    private static final float CLOUD_RADIUS = 3.0f; // 3 block radius
    private static final float CLOUD_RADIUS_PER_TICK = -0.01f; // Shrinks slightly
    private static final int CLOUD_WAIT_TIME = 5; // 0.25 seconds before it applies effects
    private static final int REAPPLICATION_DELAY = 20; // Applies effect once per second

    // Potion Effect Constants
    private static final int EFFECT_DURATION_TICKS = 4 * 20; // 4 second effect on contact (POISON, NAUSEA)
    private static final int POISON_AMPLIFIER = 0; // Level 1 Poison
    private static final int NAUSEA_AMPLIFIER = 0; // Level 1 Nausea

    // Tracker-specific constants from TearGasItem
    private static final int ACTIVATION_TICKS_THRESHOLD = TearGasItem.ACTIVATION_TICKS_THRESHOLD;
    private static final int PROC_DELAY_TICKS = TearGasItem.PROC_DELAY_TICKS;
    private static final double STATIONARY_VELOCITY_THRESHOLD = TearGasItem.STATIONARY_VELOCITY_THRESHOLD;
    private static final int FAILSAFE_DESPAWN_TICKS = TearGasItem.FAILSAFE_DESPAWN_TICKS;


    public TearGasTracker(Item item, Plugin plugin) {
        this.item = item;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Check if the item is no longer valid (removed, despawned, etc.)
        if (!item.isValid()) {
            this.cancel();
            return;
        }

        // Primary check: Has the item lived long enough to complete its flight arc?
        // This prevents immediate activation if it hits something right after being thrown.
        if (item.getTicksLived() < ACTIVATION_TICKS_THRESHOLD) {
            return;
        }

        // Secondary check: Is the item effectively stationary?
        if (item.getVelocity().lengthSquared() < STATIONARY_VELOCITY_THRESHOLD) {
            ticksStationary++;
        } else {
            // If it's moving (bouncing, sliding), reset the counter
            ticksStationary = 0;
        }

        // Final check: Has it been stationary for the required delay?
        if (ticksStationary >= PROC_DELAY_TICKS) {
            // Additional check to ensure it's "landed" on something stable
            if (item.isOnGround() || item.isInWater() || item.getVelocity().lengthSquared() < STATIONARY_VELOCITY_THRESHOLD) {
                spawnTearGasCloud(item.getLocation());
                item.remove(); // Remove the visual canister
                this.cancel(); // Stop the tracking task
            }
        }

        // FAILSAFE: Force activation/removal if it gets stuck or flies too long
        if (item.getTicksLived() > FAILSAFE_DESPAWN_TICKS) {
            spawnTearGasCloud(item.getLocation()); // Spawn cloud where it ended up
            item.remove();
            this.cancel();
        }
    }

    /**
     * Spawns and configures the AreaEffectCloud at the given location.
     */
    private void spawnTearGasCloud(Location location) {
        // Play an impact sound when the canister hits
        location.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);

        // Ensure the cloud spawns slightly above the ground if the item landed directly on a block
        Location cloudSpawnLoc = location.clone();
        Block blockAtLoc = cloudSpawnLoc.getBlock();
        if (blockAtLoc.getType().isSolid()) {
            cloudSpawnLoc.add(0, 0.5, 0); // Move up half a block to be in the air
        }
        cloudSpawnLoc.setYaw(0); // Standardize rotation
        cloudSpawnLoc.setPitch(0);


        AreaEffectCloud gasCloud = (AreaEffectCloud) location.getWorld().spawnEntity(cloudSpawnLoc, org.bukkit.entity.EntityType.AREA_EFFECT_CLOUD);

        // Configure the cloud for "Tear Gas"
//        gasCloud.setSource(item.getThrower() instanceof Player ? (Player) item.getThrower() : null); // Link to player if possible
        gasCloud.setDuration(CLOUD_DURATION_TICKS);
        gasCloud.setRadius(CLOUD_RADIUS); // Initial expanded radius
        gasCloud.setRadiusPerTick(CLOUD_RADIUS_PER_TICK); // Shrinks slightly
        gasCloud.setWaitTime(CLOUD_WAIT_TIME); // Slight delay before effects apply
        gasCloud.setReapplicationDelay(REAPPLICATION_DELAY); // How often effects reapply

        // The "Tear Gas" effects: Poison and Nausea
        PotionEffect poison = new PotionEffect(PotionEffectType.POISON, EFFECT_DURATION_TICKS, POISON_AMPLIFIER, true, false);
        PotionEffect nausea = new PotionEffect(PotionEffectType.CONFUSION, EFFECT_DURATION_TICKS, NAUSEA_AMPLIFIER, true, false);

        gasCloud.addCustomEffect(poison, true);
        gasCloud.addCustomEffect(nausea, true);

        // Visuals - Gray/White smoke-like particle
        gasCloud.setParticle(Particle.SMOKE_NORMAL);
        gasCloud.setColor(Color.fromRGB(150, 150, 150)); // Gray/White gas color
    }
}
