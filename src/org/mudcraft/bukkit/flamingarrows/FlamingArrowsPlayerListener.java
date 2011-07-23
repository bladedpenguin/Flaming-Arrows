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

import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Handles player events for the Flaming Arrows! plug-in.
 * @author Geoffrey Davis
 */
public class FlamingArrowsPlayerListener extends PlayerListener {
    /**
     * Constructs a new {@link FlamingArrowsPlayerListener} instance.
     * @param plugin the {@link FlamingArrows} object
     */
    public FlamingArrowsPlayerListener(FlamingArrows plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Gets the number of flint & steel charges a {@link Player} has
     * available across all the flint & steel {@link ItemStack}s in their
     * inventory.
     * @param player the {@link Player} object
     * @return the number of flint & steel charges, or <code>0</code>
     * @see #getFlintAndSteelCharges()
     */
    private int getFlintAndSteelDurability(Player player) {
        // The total durability across all flint & steel item stacks
        // in the player's inventory.
        int totalDurability = 0;
        
        // Iterate over the item stacks in the player's inventory.
        for (ItemStack itemStack: player.getInventory().getContents()) {
            // Make certain there's an item in the slot.
            if (itemStack == null)
                continue;
            
            // Skip slots that do not contain flint and steel.
            if (itemStack.getType() != Material.FLINT_AND_STEEL)
                continue;
            
            // Skip item stacks that are already used up.
            if (itemStack.getDurability() >= 64)
                continue;

            // Add the number of charges that are still available.
            totalDurability += (64 - itemStack.getDurability());
            
            // Does the item stack contains multiple items?
            if (itemStack.getAmount() > 1) {
                // Add 64 charges for each additional item.
                totalDurability += (itemStack.getAmount() - 1) * 64;
            }
        }
        return totalDurability;
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
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Save a reference to the player.
        final Player p = event.getPlayer();
        // To use Flaming Arrows! the player must be an operator or be
        // on the player white-list.
        //if (!p.isOp() && !getPlugin().getSettings().isPlayerWhitelisted(p) && !FlamingArrows.permissionHandler.has(p, "FlamingArrows.ignite"))
        //    return;
        
        // Save a reference to the item stack in the player's hand.
        final ItemStack itemStack = event.getItem();
        
        switch (event.getAction()) {
        case LEFT_CLICK_AIR:
        case LEFT_CLICK_BLOCK:
            // The item in the player's hand must be flint & steel.
            if (itemStack == null ||
                itemStack.getType() != getPlugin().getSettings().getWand())
                return;
            if (!FlamingArrows.permissionHandler.has(p,"FlamingArrows.ignite"))
            	return;
            
            
            // First we'll try to remove the player.
            // If that doesn't work, we know the player was not present
            // in the list, so they're safe to be added.
            // Redone by bladedpenguin to inform the player they are out of flint and steel
            if (getPlugin().getArrowManager().getPlayers().remove(p) == false)
            	if (getFlintAndSteelDurability(p) < getPlugin().getSettings().getFlintAndSteelDurabilityCost())
            		p.sendMessage(getPlugin().getSettings().getRanOutMessage());
            	else {
            		getPlugin().getArrowManager().getPlayers().add(p);
            		p.sendMessage(getPlugin().getSettings().getEnabledMessage());
            	}
            else
            	p.sendMessage(getPlugin().getSettings().getDisabledMessage());
            		
            // Cancel the event.
            event.setCancelled(true);
            break;
        case RIGHT_CLICK_AIR:
        case RIGHT_CLICK_BLOCK:
            // The item in the player's hand must be a bow.
            if (itemStack == null || itemStack.getType() != Material.BOW)
                return;
            
            // The player must have enabled flaming arrows.
            if (!getPlugin().getArrowManager().getPlayers().contains(p))
                return;
            
            // The player must have at least one arrow.
            if (!p.getInventory().contains(Material.ARROW))
                return;
            
            // The player must have at least one flint & steel.
            // bladedpenguin says they should know when they are out
            if (getFlintAndSteelDurability(p) < getPlugin().getSettings().getFlintAndSteelDurabilityCost()){
            	getPlugin().getArrowManager().getPlayers().remove(p);
            	p.sendMessage(getPlugin().getSettings().getRanOutMessage());
            	return;
            }
            	

            // Remove one arrow from the player's inventory.
            p.getInventory().removeItem(new ItemStack(Material.ARROW, 1));

            // Deduct flint & steel charges from the player.
            removeFlintAndSteelCharges(p);
            
            // Cause the player to fire a flaming arrow.
            p.shootArrow().setFireTicks(600);
            
            // Cancel the event.
            event.setCancelled(true);
            break;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        getPlugin().getArrowManager().getPlayers().remove(event.getPlayer());
    }
    
    /**
     * Removes the correct number of flint & steel durability points and
     * {@link ItemStack}s from a {@link Player}'s inventory.
     * @param player the {@link Player} object
     * @param charges the number of charges to be subtracted
     */
    void removeFlintAndSteelCharges(Player player) {
        // Save off a reference to the player's inventory.
        final Inventory inventory = player.getInventory();
        
        // Get the number of required flint & steel charges.
        int numberOfCharges =
            getPlugin().getSettings().getFlintAndSteelDurabilityCost();
        
        // Loop until we've subtracted enough charges.
        while (numberOfCharges-- > 0) {
            // Search for flint and steel in the player's inventory.
            // This method returns the slot the item occupies.
            final int slotNumber = inventory.first(Material.FLINT_AND_STEEL);
            
            // We couldn't find any more flint and steel.
            if (slotNumber == -1)
                break;
            
            // Get the item stack in the slot we found.
            final ItemStack itemStack = inventory.getItem(slotNumber);
            
            // Save off the durability of the item stack.
            int durability = itemStack.getDurability();
            
            // Check whether the current item stack has additional
            // durability available.
            if (durability + 1 >= 64) {
                // Check the size of the item stack.
                if (itemStack.getAmount() > 1) {
                    // Reduce the size of the item stack by one.
                    itemStack.setAmount(itemStack.getAmount() - 1);
                    // Set the item stack's durability back to zero.
                    itemStack.setDurability((short) 0);
                } else {
                    // Remove the item stack entirely.
                    inventory.setItem(slotNumber, null);        
                }
            } else {
                // Reduce the durability of the item stack.
                itemStack.setDurability((short) (durability + 1));
            }
        }
        // Update the player's inventory.
        ((CraftPlayer) player).updateInventory();
    }
    
    /**
     * The {@link FlamingArrows} object
     * @see #getPlugin()
     */
    private FlamingArrows plugin;
}
