package io.th0rgal.oraxen.recipes.builders;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class StonecuttingBuilder extends RecipeBuilder {

    @Override
    Inventory createInventory(Player player, String inventoryTitle) {
        return Bukkit.createInventory(player, InventoryType.WORKBENCH, "<glyph:recipe_stonecutter>");
    }

    public StonecuttingBuilder(Player player) {
        super(player, "stonecutting");
    }

    @Override
    public void saveRecipe(String name) {
        saveRecipe(name, null);
    }

    @Override
    public void saveRecipe(String name, String permission) {

        ItemStack input = getInventory().getItem(0);
        
        // Clear old stonecutting recipes with this name pattern
        var config = getConfig();
        config.getKeys(false).stream()
                .filter(key -> key.startsWith(name + "_"))
                .forEach(key -> config.set(key, null));
        
        int recipeCount = 0;
        for (int i = 1; i < getInventory().getSize(); i++) {
            ItemStack result = getInventory().getItem(i);
            if (result == null) continue;
            String recipeKey = name + "_" + recipeCount;
            ConfigurationSection newCraftSection;
            if (config.isConfigurationSection(recipeKey)) {
                newCraftSection = config.getConfigurationSection(recipeKey);
                newCraftSection.set("result", null);
                newCraftSection.set("input", null);
            } else {
                newCraftSection = config.createSection(recipeKey);
            }
            
            setSerializedItem(newCraftSection.createSection("result"), result);
            setSerializedItem(newCraftSection.createSection("input"), input);

            if (permission != null && !permission.isEmpty()) newCraftSection.set("permission", permission);

            saveConfig();
            recipeCount++;
        }
        close();
    }
}
