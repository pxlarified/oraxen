package io.th0rgal.oraxen.recipes.loaders;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.StonecuttingRecipe;

public class StonecuttingLoader extends RecipeLoader {
	public StonecuttingLoader(ConfigurationSection section) {
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

		StonecuttingRecipe recipe = new StonecuttingRecipe(getNamespacedKey(), result, recipeChoice);
		loadRecipe(recipe);
	}
}
