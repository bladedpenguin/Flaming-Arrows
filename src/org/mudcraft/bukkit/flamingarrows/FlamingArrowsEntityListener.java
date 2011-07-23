/**
 * @file FlamingArrows.java
 * 
 * Copyright (C) 2011 MUDCraft.org
 * All Rights Reserved.
 *
 * @author Geoffrey Davis
 *
 * $Id$
 */
package org.mudcraft.bukkit.flamingarrows;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles entity events for the Flaming Arrows! plug-in.
 * @author Geoffrey Davis
 */
public class FlamingArrowsEntityListener extends EntityListener {
    /**
     * Constructs a new {@link FlamingArrowsEntityListener} instance.
     * @param plugin the {@link FlamingArrows} object
     */
    public FlamingArrowsEntityListener(FlamingArrows plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Gets the plug-in.
     * @return the {@link FlamingArrows} object
     */
    public FlamingArrows getPlugin() {
        return plugin;
    }
    
    /**
     * {@inheritDoc}
     */
    public void onEntityDamage(EntityDamageEvent event) {
        // The pig mobile if it was the target.
        Pig pig = null;
        
        // Check whether a pig was the target of the event.
        if (event.getEntity() instanceof Pig) {
            pig = (Pig) event.getEntity();
        }
                    
        // Make certain we're handling a projectile event for
        // a player or mobile (living entities).
        if (event instanceof EntityDamageByProjectileEvent &&
            event.getEntity() instanceof LivingEntity) {
            // Cast the event to an EntityDamagedByProjectileEvent.
            final EntityDamageByProjectileEvent realEvent =
                (EntityDamageByProjectileEvent) event;
            
            // Make certain we're handling an arrow and the arrow
            // is presently flaming.
            if (realEvent.getProjectile() instanceof Arrow &&
                realEvent.getProjectile().getFireTicks() > 0) {
                // Cast the projectile's target to a LivingEntity.
                final LivingEntity target = (LivingEntity) event.getEntity();

                // This is the number of fire ticks.
                int fireTicks = 0;
                
                // Check whether we're handling a player or non-player.
                if (target instanceof Player) {
                    // Record the number of player fire ticks.
                    fireTicks = getPlugin().getSettings().getPlayerFireTicks();
                } else {
                    // Record the number of non-player fire ticks.
                    fireTicks = getPlugin().getSettings().getNonPlayerFireTicks();
                }
                if (fireTicks != 0) {
                    // Get the target's current fire ticks.
                    int currentFireTicks = target.getFireTicks();
                    
                    // Don't add more fire ticks then they already have.
                    if (currentFireTicks < fireTicks) {
                        // Set the target on fire for some number of ticks.
                        target.setFireTicks(fireTicks);
                    }
                    if (pig != null) {
                        // Add the pig mobile to the bacon list.
                        bacon.add(pig);
                    }
                }
            } else {
                if (pig != null && pig.getFireTicks() == 0) {
                    // Remove the pig mobile from the bacon list.
                    bacon.remove(pig);
                }
            }
        } else if (event.getCause() == DamageCause.FIRE ||
                   event.getCause() == DamageCause.FIRE_TICK ||
                   event.getCause() == DamageCause.LAVA) {
            if (pig != null) {
                // Add the pig mobile to the bacon list.
                bacon.add(pig);
            }
        } else {
            if (pig != null && pig.getFireTicks() == 0) {
                // Remove the pig mobile from the bacon list.
                bacon.remove(pig);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void onEntityDeath(EntityDeathEvent event) {
        // Was this event triggered by the death of a pig?
        if (event.getEntity() instanceof Pig) {
            // Cast the entity to a pig.
            Pig pig = (Pig) event.getEntity();
            
            // If the pig was in the bacon list, cook its pork.
            if (bacon.remove(pig)) {
                // Iterate over the pigs item drops.
                for (ItemStack item: event.getDrops()) {
                    // Was the drop a piece of pork?
                    if (item.getType() == Material.PORK) {
                        // Set the item drop to grilled pork.
                        item.setType(Material.GRILLED_PORK);
                    }
                }
            }
        }else if (event.getEntity() instanceof Arrow){
        	Arrow arrow = (Arrow) event.getEntity();
        	if (arrow.getFireTicks() > 0) {
        		((Player) arrow.getShooter()).sendMessage("Your firearrow died!");
        	}
        }
    }
    public void onProjectileHit(ProjectileHitEvent event) {
    	if (!(event.getEntity() instanceof Arrow))
    		return;
    	Arrow arrow = (Arrow)event.getEntity();
    	if (arrow.getFireTicks() <= 0)
    		return;
    	Block block = arrow.getLocation().getBlock();
    	if (block.getType() == org.bukkit.Material.AIR){
    		block.setType(org.bukkit.Material.FIRE);
    		return;
    	}else {
    		FlamingArrows.logger().info("FireArrow landed in solid block");
    		if (arrow.getShooter() instanceof Player ){
    			Player player = (Player) arrow.getShooter();
    			player.sendMessage("Please Report: FireArrow landed in solid block");
    		}
    	}
    	
    }
    
    /**
     * Maintains a list of the pigs damaged by fire.  We track this
     * so we can automatically cook the pork dropped by pigs killed by
     * fire. 
     */
    private final List<Pig> bacon = new ArrayList<Pig>();
    
    /**
     * The {@link FlamingArrows} object
     * @see #getPlugin()
     */
    private FlamingArrows plugin;
}
