/**
 * @file ArrowManager.java
 * 
 * Copyright (C) 2011 MUDCraft.org
 * All Rights Reserved.
 *
 * @author Geoffrey Davis
 *
 * $Id$
 */
package org.mudcraft.bukkit.flamingarrows;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

/**
 * @author Geoffrey Davis
 */
public class ArrowManager {
    /**
     * Constructs a new {@link ArrowManager} instance.
     * @param plugin the {@link FlamingArrows} object
     */
    public ArrowManager(FlamingArrows plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Gets the {@link Player}s who presently have flaming arrows enabled.
     * @return a {@link Set} of {@link Player}s
     */
    public Set<Player> getPlayers() {
        return players;
    }
    
    /**
     * Gets the plug-in.
     * @return the {@link FlamingArrows} object
     */
    public FlamingArrows getPlugin() {
        return plugin;
    }
    
    /**
     * Contains the {@link Player}s who presently have flaming arrows enabled.
     * @see #getPlayers()
     */
    private final Set<Player> players = new HashSet<Player>();
    
    /**
     * The {@link FlamingArrows} object
     * @see #getPlugin()
     */
    private final FlamingArrows plugin;
}
