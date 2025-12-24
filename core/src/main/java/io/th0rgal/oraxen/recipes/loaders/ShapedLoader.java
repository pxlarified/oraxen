package io.th0rgal.oraxen.recipes.loaders;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;
import java.util.Objects;

public class ShapedLoader extends RecipeLoader {

    public ShapedLoader(ConfigurationSection section) {
        super(section);
    }

    @Override
    public void registerRecipe() {
        var result = getResult();
        if (result == null) throw new NullPointerException("Result is null or invalid");

        ShapedRecipe recipe = new ShapedRecipe(getNamespacedKey(), result);

        List<String> shape = getSection().getStringList("shape");
        if (shape.isEmpty()) throw new IllegalArgumentException("Shape is empty");
        recipe.shape(shape.toArray(new String[0]));

        ConfigurationSection ingredientsSection = getSection().getConfigurationSection("ingredients");
        if (ingredientsSection == null) throw new NullPointerException("Ingredients section is missing");

        for (String ingredientLetter : ingredientsSection.getKeys(false)) {
            ConfigurationSection itemSection = ingredientsSection.getConfigurationSection(ingredientLetter);
            if (itemSection == null) continue;
            RecipeChoice recipeChoice = getRecipeChoice(itemSection);
            if (recipeChoice == null) continue;
            recipe.setIngredient(ingredientLetter.charAt(0), recipeChoice);
        }
        addToWhitelistedRecipes(recipe);
        loadRecipe(recipe);
    }
}
