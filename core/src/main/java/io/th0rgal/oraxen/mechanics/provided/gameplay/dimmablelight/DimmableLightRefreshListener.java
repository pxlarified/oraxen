package io.th0rgal.oraxen.mechanics.provided.gameplay.dimmablelight;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenFurniture;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.stringblock.StringBlockMechanic;
import io.th0rgal.oraxen.utils.BlockHelpers;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static io.th0rgal.oraxen.mechanics.provided.gameplay.dimmablelight.DimmableLightMechanic.DIMMABLE_LIGHT_LEVEL_KEY;

public class DimmableLightRefreshListener implements Listener {

    private final DimmableLightMechanicFactory factory;

    public DimmableLightRefreshListener(DimmableLightMechanicFactory factory) {
        this.factory = factory;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerLoad(ServerLoadEvent event) {
        // Refresh all dimmable lights on server load/reload
        OraxenPlugin.get().getServer().getScheduler().runTaskLater(OraxenPlugin.get(), this::refreshAllDimmableLights, 20L);
    }

    private void refreshAllDimmableLights() {
        for (String itemID : factory.getItems()) {
            Mechanic mechanic = factory.getMechanic(itemID);
            if (!(mechanic instanceof DimmableLightMechanic dimmableLight)) continue;

            // Refresh furniture
            for (String furnitureID : OraxenFurniture.getFurnitureIDs()) {
                if (furnitureID.equals(itemID)) {
                    refreshFurnitureLights(furnitureID, dimmableLight);
                }
            }

            // Refresh noteblocks
            NoteBlockMechanic noteBlockMechanic = OraxenBlocks.getNoteBlockMechanic(itemID);
            if (noteBlockMechanic != null) {
                refreshNoteBlockLights(itemID, dimmableLight);
            }

            // Refresh stringblocks
            StringBlockMechanic stringBlockMechanic = OraxenBlocks.getStringMechanic(itemID);
            if (stringBlockMechanic != null) {
                refreshStringBlockLights(itemID, dimmableLight);
            }
        }
    }

    private void refreshFurnitureLights(String itemID, DimmableLightMechanic dimmableLight) {
        // This would require iterating through all loaded chunks/entities
        // For now, we'll refresh on-demand when furniture is interacted with
    }

    private void refreshNoteBlockLights(String itemID, DimmableLightMechanic dimmableLight) {
        // This would require iterating through all loaded chunks
        // For now, we'll refresh on-demand when blocks are interacted with
    }

    private void refreshStringBlockLights(String itemID, DimmableLightMechanic dimmableLight) {
        // This would require iterating through all loaded chunks
        // For now, we'll refresh on-demand when blocks are interacted with
    }

    public static void refreshLight(Block block, Entity entity, DimmableLightMechanic dimmableLight) {
        PersistentDataContainer pdc = null;
        if (block != null) {
            pdc = BlockHelpers.getPDC(block);
        } else if (entity != null) {
            pdc = entity.getPersistentDataContainer();
            // Get block from entity location if block is null
            if (entity.getLocation().getBlock() != null) {
                block = entity.getLocation().getBlock();
            }
        }
        if (pdc == null || block == null) return;

        int lightLevel = pdc.getOrDefault(DIMMABLE_LIGHT_LEVEL_KEY, PersistentDataType.INTEGER, dimmableLight.getDefaultLightLevel());
        
        // Remove old lights
        removeLightBlocks(block);

        // Create new lights
        if (lightLevel > 0) {
            createLightBlocks(block, lightLevel);
        }

        // For furniture with barriers
        if (entity != null) {
            FurnitureMechanic furnitureMechanic = OraxenFurniture.getFurnitureMechanic(entity);
            if (furnitureMechanic != null && furnitureMechanic.hasBarriers(entity)) {
                Location baseLocation = block.getLocation();
                for (io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.BlockLocation barrierLoc : furnitureMechanic.getBarriers(entity)) {
                    Block barrierBlock = barrierLoc.add(baseLocation).getBlock();
                    if (barrierBlock != null) {
                        removeLightBlocks(barrierBlock);
                        if (lightLevel > 0) {
                            createLightBlocks(barrierBlock, lightLevel);
                        }
                    }
                }
            }
        }
    }

    private static void removeLightBlocks(Block block) {
        BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.SELF};
        for (BlockFace face : faces) {
            Block relative = block.getRelative(face);
            if (relative.getType() == Material.LIGHT) {
                relative.setType(Material.AIR);
            }
        }
    }

    private static void createLightBlocks(Block block, int lightLevel) {
        Light lightData = (Light) Material.LIGHT.createBlockData();
        lightData.setLevel(lightLevel);

        BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.SELF};
        for (BlockFace face : faces) {
            Block relative = block.getRelative(face);
            if (relative.getType().isAir() || relative.getType() == Material.LIGHT) {
                if (relative.getBlockData() instanceof Light existingLight && existingLight.getLevel() > lightLevel) {
                    continue;
                }
                relative.setBlockData(lightData);
            }
        }
    }
}

