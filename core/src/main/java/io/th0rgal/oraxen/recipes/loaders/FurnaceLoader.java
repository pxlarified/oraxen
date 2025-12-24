package io.th0rgal.oraxen.recipes.loaders;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.RecipeChoice;

public class FurnaceLoader extends RecipeLoader {

    public FurnaceLoader(ConfigurationSection section) {
        super(section);
    }

    @Override
    public void registerRecipe() {
        ConfigurationSection inputSection = getSection().getConfigurationSection("input");
        if (inputSection == null) throw new NullPointerException("Input section is missing");
        RecipeChoice recipeChoice = getRecipeChoice(inputSection);
        if (recipeChoice == null) throw new NullPointerException("Input ingredient is invalid");

        var result = getResult();
        if (result == null) throw new NullPointerException("Result is null or invalid");

        FurnaceRecipe recipe = new FurnaceRecipe(getNamespacedKey(), result,
                recipeChoice, getSection().getInt("experience"), getSection().getInt("cookingTime"));
        loadRecipe(recipe);
    }
}
