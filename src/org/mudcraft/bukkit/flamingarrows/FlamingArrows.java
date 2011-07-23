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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * @author Geoffrey Davis
 */
public class FlamingArrows extends JavaPlugin {
    /**
     * Sets up the plug-in configuration file.
     */
	public static PermissionHandler permissionHandler;
	
    private void doConfigurationFile() {
        // Create the directory structure for the plug-in.
        getDataFolder().mkdirs();
        
        // Create a File representing the config.yml configuration file.
        final File configFile = new File(getDataFolder(), "config.yml");
        
        // If the file doesn't exist create one.
        if (configFile.exists() == false) {
            FileWriter fileWriter = null;
            try {
                // Create a FileWriter object.
                fileWriter = new FileWriter(configFile);
                
                // Write the default configuration file.
                fileWriter.write(
                        "flaming-arrows:\n" +
                        "  charges-required:\n" +
                        "    flint-and-steel: 5\n" +
                        "  fire-ticks:\n" +
                        "    non-player: 600\n" +
                        "    player: 0\n" +
                        "  messages:\n" +
                        "    disabled: '*Flaming Arrows* You are now firing normal arrows.'\n" +
                        "    enabled: '*Flaming Arrows* You are now firing flaming arrows.'\n" +
                        "  wand: bow\n" +
                        "  whitelist:\n" +
                        "    - '*'\n");
            } catch (Exception ex) {
                // Throw an exception to indicate an error.
                throw new RuntimeException(ex.getMessage(), ex);
            } finally {
                if (fileWriter != null) {
                    try {
                        // Close the file writer.
                        fileWriter.close();
                    } catch (IOException ex) {
                        if (logger().isLoggable(Level.WARNING)) {
                            // Write a warning message to the logger.
                            logger().warning(ex.getMessage());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Gets the plug-in's arrow manager.
     * @return an {@link ArrowManager} object
     */
    public ArrowManager getArrowManager() {
        if (arrowManager == null) {
            // Create a new ArrowManager instance.
            arrowManager = new ArrowManager(this);
        }
        return arrowManager;
    }
    
    /**
     * Gets the plug-in's settings.
     * @return an {@link ArrowSettings} object
     */
    public ArrowSettings getSettings() {
        return settings;
    }
    
    /**
     * Gets a suitable logger for this class.
     * @return a <code>Logger</code> object
     */
    protected static Logger logger() {
        return Logger.getLogger("Minecraft");
    }
    
    /**
     * {@inheritDoc}
     */
    public void onDisable() {
        // Clear any configured players.
        getArrowManager().getPlayers().clear();
        
        // Obtain a plug-in descriptor for this plug-in.
        final PluginDescriptionFile pdf = getDescription();
        
        if (logger().isLoggable(Level.INFO)) {
            // Write an informational message to the logger.
            logger().info("Disabled " + pdf.getName() + "!");
        }
    }

    /**
     * {@inheritDoc}
     */
    private void setupPermissions() {
        if (permissionHandler != null) {
            return;
        }
        
        Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
        
        if (permissionsPlugin == null) {
            logger().info("Permission system not detected, defaulting to OP");
            return;
        }
        
        permissionHandler = ((Permissions) permissionsPlugin).getHandler();
        logger().info("Found and will use plugin "+((Permissions)permissionsPlugin).getDescription().getFullName());
    }
    
    public void onEnable() {
        // Sets up the plug-in configuration file.
        doConfigurationFile();
        
        // Obtain a PluginManager instance to register hooks.
        final PluginManager pm = getServer().getPluginManager();
        
        // Register events we care about with the plug-in manager.
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PROJECTILE_HIT, entityListener, Priority.Normal, this);
        
        setupPermissions();
        
        // Obtain a plug-in descriptor for this plug-in.
        final PluginDescriptionFile pdf = getDescription();
        
        if (logger().isLoggable(Level.INFO)) {
            // Write an informational message to the logger.
            logger().info("Enabled " + pdf.getName() + "!");
            logger().info(" - Author(s): " + pdf.getAuthors());
            logger().info(" - Version: " + pdf.getVersion());
        }
        
        // Reload the configuration settings.
        getSettings().readConfiguration();
    }
    
    /**
     * The {@link ArrowManager} instance.
     * @see #getArrowManager()
     */
    private ArrowManager arrowManager;
    
    /**
     * Handles entity events for the FlamingArrows plug-in.
     */
    private final EntityListener entityListener = new FlamingArrowsEntityListener(this);
    
    /**
     * Handles player events for the FlamingArrows plug-in.
     */
    private final PlayerListener playerListener = new FlamingArrowsPlayerListener(this);
    
    /**
     * The plug-in settings.
     * @see #getSettings()
     */
    private final ArrowSettings settings = new ArrowSettings(this);
}
