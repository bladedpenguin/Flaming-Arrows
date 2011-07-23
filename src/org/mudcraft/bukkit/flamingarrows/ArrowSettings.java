/**
 * @file ArrowSettings.java
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
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Geoffrey Davis
 */
public class ArrowSettings {
    /**
     * Constructs a new {@link ArrowManager} instance.
     * @param plugin the {@link FlamingArrows} object
     */
    public ArrowSettings(FlamingArrows plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Gets the message displayed when Flaming Arrows! is disabled.
     * @return the disabled message
     */
    public String getDisabledMessage() {
        return disabledMessage;
    }

    /**
     * Gets the message displayed when Flaming Arrows! is enabled.
     * @return the enabled message
     */
    public String getEnabledMessage() {
        return enabledMessage;
    }
    
    public String getRanOutMessage() {
		return ranOutMessage;
    }

    /**
     * Gets the flint & steel durability cost.
     * @return the  the number of units of flint & steel durability to
     *      consume for each flaming arrow released
     */
    public int getFlintAndSteelDurabilityCost() {
        return flintAndSteelDurabilityCost;
    }
    
    /**
     * Gets the number of Minecraft ticks for which {@link Player}
     * entities burn once struck by a flaming arrow.
     * @return the number of player fire ticks
     */
    public int getPlayerFireTicks() {
        return playerFireTicks;
    }
    
    /**
     * Gets the number of Minecraft ticks for which non-{@link Player}
     * entities burn once struck by a flaming arrow.
     * @return the number of player fire ticks
     */
    public int getNonPlayerFireTicks() {
        return nonPlayerFireTicks;
    }
    
    /**
     * Gets the plug-in.
     * @return the {@link FlamingArrows} object
     */
    public FlamingArrows getPlugin() {
        return plugin;
    }
    
    /**
     * Gets the {@link Player}s who are permitted to use Flaming Arrows!
     * @return a {@link List} of {@link Player} names
     */
    public List<String> getPlayerWhitelist() {
        return playerWhitelist;
    }
    
    /**
     * Gets the "wand" used to enable and disable Flaming Arrows!
     * @return the wand {@link Material}
     */
    public Material getWand() {
        return wand;
    }
    
    /**
     * Gets whether a {@link Player} is on the white-list.
     * @param player the {@link Player} object
     * @return <code>true</code> if the specified {@link Player} is on
     *      the white-list
     */
    public boolean isPlayerWhitelisted(Player player) {
        return getPlayerWhitelist().contains("*") ||
               getPlayerWhitelist().contains(player.getName().toLowerCase());
    }
    
    /**
     * Searches for the wand {@link Material} by name.
     * @param wandName the name of the wand {@link Material}
     * @return the {@link Material} or {@link Material#BOW}
     */
    private static Material parseWandMaterial(String wandName) {
        // Convert the wand name to lowercase. 
        wandName = wandName.trim().toLowerCase();
        
        // Iterate over the available materials.
        for (Material m: Material.values()) {
            // We're going to use this quite a lot.
            final String mName = m.name().trim().toLowerCase();
            
            // Compare the raw name of the material.
            if (mName.equals(wandName))
                return m;
            // Compare the name without the underscores.
            if (mName.replace("_", "").equals(wandName))
                return m;
            // Compare the name with hyphens for the underscores.
            if (mName.replace("_", "-").equals(wandName))
                return m;
            // Compare the name with whitespace for the underscores.
            if (mName.replace("_", " ").equals(wandName))
                return m;
        }
        return Material.BOW;
    }
    
    /**
     * Reads the settings from the plug-in's configuration.
     */
    public void readConfiguration() {
        // Reload the plug-in configuration.
        getPlugin().getConfiguration().load();
        
        /*
         * We need to handle old-format configuration files.
         */
        
        // Read fire ticks from the configuration file.
        nonPlayerFireTicks = playerFireTicks =
            getPlugin().getConfiguration().getInt(
                "flaming-arrows.ignition.fire-ticks",
                600);
        
        // Check whether player ignition was enabled.
        if (getPlugin().getConfiguration().getBoolean(
                "flaming-arrows.ignition.ignite-players", false) == false) {
            // Reset player fire ticks to zero.
            playerFireTicks = 0;
        }
        
        /*
         * Now handle new format configuration files.
         */

        // Read disabled message from the configuration file.
        disabledMessage = getPlugin().getConfiguration().getString(
                "flaming-arrows.messages.disabled",
                "*Flaming Arrows* You are now firing normal arrows.");
        
        // Read enabled message from the configuration file.
        enabledMessage = getPlugin().getConfiguration().getString(
                "flaming-arrows.messages.enabled",
                "*Flaming Arrows* You are now firing flaming arrows.");
        
        ranOutMessage = getPlugin().getConfiguration().getString(
                "flaming-arrows.messages.ran-out",
        		"*Flaming Arrows* You don't have enough Flint & Steel");
        
        // Read flint & steel durability cost from the configuration file.
        flintAndSteelDurabilityCost = getPlugin().getConfiguration().getInt(
                "flaming-arrows.charges-required.flint-and-steel",
                5);

        // Read non-player fire ticks from the configuration file.
        nonPlayerFireTicks = getPlugin().getConfiguration().getInt(
                "flaming-arrows.fire-ticks.non-player",
                nonPlayerFireTicks);

        // Read player fire ticks from the configuration file.
        playerFireTicks = getPlugin().getConfiguration().getInt(
                "flaming-arrows.fire-ticks.player",
                playerFireTicks);
        
        // Read the player white-list from the configuration file.
        playerWhitelist = getPlugin().getConfiguration().getStringList(
                "flaming-arrows.whitelist",
                new ArrayList<String>(Collections.nCopies(1, "*"))
                );
        
        // Read the wand material from the configuration file.
        wand = parseWandMaterial(
            getPlugin().getConfiguration().getString(
                "flaming-arrows.wand",
                "BOW")
            );
        
        // Check the range of non-player fire ticks.
        nonPlayerFireTicks = Math.max(Math.min(nonPlayerFireTicks, 600), 0);

        // Iterate over the list of white-listed player names.
        for (int i = 0; i < playerWhitelist.size(); ++i) {
            // Get the name of the player from the white-list.
            final String playerName = playerWhitelist.get(i);
            
            // Normalize the player's name.
            playerWhitelist.set(i, playerName.trim().toLowerCase());
        }
        
        // Check the range of player fire ticks.
        playerFireTicks = Math.max(Math.min(playerFireTicks, 600), 0);
        
        // Check the range of flint & steel durability cost.
        flintAndSteelDurabilityCost =
            Math.max(Math.min(flintAndSteelDurabilityCost, 64), 0);
    }
    
    /**
     * This message is displayed when flaming arrows are disabled.
     * @see #getDisabledMessage()
     */
    private String disabledMessage =
        "*Flaming Arrows* You are now firing normal arrows.";

    /**
     * This message is displayed when flaming arrows are enabled.
     * @see #getEnabledMessage()
     */
    private String enabledMessage =
        "*Flaming Arrows* You are now firing flaming arrows.";

    private String ranOutMessage = "*Flaming Arrows* You don't have enough Flint & Steel";
    /*
     * bladedpenguin says: I'll try to copy your style, but docs are for chumps.
     * specifically, documenting the obvious decreases the signal to noise ratio 
     * of your code. You chose good variable names. Let it stop there. 
     * 
     * It's not that I'm lazy. I would not ahve taken the time to write this, if I were
     * */ 
    
    
    /**
     * The number of units of flint & steel durability to consume for
     * each flaming arrow released.
     * @see #getFlintAndSteelDurabilityCost()
     */
    private int flintAndSteelDurabilityCost = 5;
    
    /**
     * The number of Minecraft ticks for which non-{@link Player} entities
     * burn once struck by a flaming arrow.
     * @see #getNonPlayerFireTicks()
     */
    private int nonPlayerFireTicks = 600;
    
    /**
     * The number of Minecraft ticks for which {@link Player} entities
     * burn once struck by a flaming arrow.
     * @see #getPlayerFireTicks()
     */
    private int playerFireTicks = 0;
    
    /**
     * The {@link Player}s who are permitted to use Flaming Arrows!
     * @see #getPlayerWhitelist();
     */
    private List<String> playerWhitelist = new ArrayList<String>();

    /**
     * The {@link FlamingArrows} object
     * @see #getPlugin()
     */
    private final FlamingArrows plugin;
    
    /**
     * The "wand" used to enable and disable Flaming Arrows!
     * @see #getWand()
     */
    private Material wand = Material.BOW;
    
    
}
