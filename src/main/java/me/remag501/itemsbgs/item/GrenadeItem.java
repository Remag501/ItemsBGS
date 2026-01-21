package me.remag501.itemsbgs.item;

import me.remag501.itemsbgs.model.AbstractTargetingItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

/**
 * Concrete implementation of the Frag Grenade item.
 * Extends AbstractTargetingItem to use the scalable Right-Click activation structure.
 */
public class GrenadeItem extends AbstractTargetingItem {

    private static final String ID = "grenade";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ItemStack getItem(int amount) {
        // Grenades use COBBLESTONE for their appearance
        ItemStack item = new ItemStack(Material.COBBLESTONE, amount);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§b§lFrag Grenade");
        meta.setLore(Arrays.asList(
                "§7A simple timed explosive that",
                "§7causes a small, non-destructive blast.",
                "",
                "§bRight-click to use." // Consistent with Molotov and new listener
        ));

        // The manager handles setting the PersistentDataContainer ID
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Executes the Grenade effect logic after consumption and target validation.
     * This method is guaranteed to run on a valid target location.
     */
    @Override
    public void onThrow(Player activator, Location targetLocation, Plugin plugin) {
        activator.sendMessage("§bGrenade thrown!");

        // Play a small explosion sound
        targetLocation.getWorld().playSound(targetLocation, Sound.ENTITY_GENERIC_EXPLODE, 3.0F, 1.5F);

        // Create a non-destructive explosion (power 2.0F, setFire: false, breakBlocks: false)
        targetLocation.getWorld().createExplosion(targetLocation, 2.0F, false, false);
    }
}
