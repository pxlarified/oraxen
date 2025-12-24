package io.th0rgal.oraxen.recipes.loaders;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.RecipeChoice;

public class BlastingLoader extends RecipeLoader {
	public BlastingLoader(ConfigurationSection section) {
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

		BlastingRecipe recipe = new BlastingRecipe(getNamespacedKey(), result,
				recipeChoice, getSection().getInt("experience"), getSection().getInt("cookingTime"));
		loadRecipe(recipe);
	}
}
