package io.th0rgal.oraxen.mechanics.provided.gameplay.blockplacing;

import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.block.BlockRegistryMirror;
import io.th0rgal.oraxen.block.ImmutableBlockState;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.utils.BlockHelpers;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockPlacingListener implements Listener {

    private static final NamespacedKey STATE_ID_KEY = new NamespacedKey(
        io.th0rgal.oraxen.OraxenPlugin.get(),
        "custom_block_state_id"
    );

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (item == null || !OraxenItems.exists(item)) return;

        String itemId = OraxenItems.getIdByItem(item);
        BlockPlacingMechanic mechanic = (BlockPlacingMechanic) MechanicsManager.getMechanicFactory("block_placing").getMechanic(itemId);
        if (mechanic == null) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Block targetBlock = event.getClickedBlock();
        if (targetBlock == null) return;

        BlockFace face = event.getBlockFace();
        Block placeBlock = targetBlock.getRelative(face);

        if (!placeBlock.getType().isAir() && placeBlock.getType().name().contains("WATER")
            && placeBlock.getType().name().contains("LAVA")) {
            return;
        }

        ImmutableBlockState state = BlockRegistryMirror.getById(mechanic.getBlockStateId());
        if (state == null) {
            Logs.logError("Block state with ID " + mechanic.getBlockStateId() + " not found");
            return;
        }

        try {
            placeCustomBlock(placeBlock, state);
            if (player.getGameMode() != GameMode.CREATIVE) {
                item.setAmount(item.getAmount() - 1);
            }
        } catch (Exception e) {
            Logs.logError("Failed to place custom block: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void placeCustomBlock(Block block, ImmutableBlockState state) {
        org.bukkit.Material visualMaterial = state.getSettings().getVisualMaterial();
        block.setType(visualMaterial, false);

        PersistentDataContainer pdc = BlockHelpers.getPDC(block);
        pdc.set(STATE_ID_KEY, PersistentDataType.INTEGER, state.getInternalId());
    }

    public static ImmutableBlockState getCustomBlockState(Block block) {
        PersistentDataContainer pdc = BlockHelpers.getPDC(block);

        if (!pdc.has(STATE_ID_KEY, PersistentDataType.INTEGER)) {
            return null;
        }

        int stateId = pdc.get(STATE_ID_KEY, PersistentDataType.INTEGER);
        return BlockRegistryMirror.getById(stateId);
    }
}
