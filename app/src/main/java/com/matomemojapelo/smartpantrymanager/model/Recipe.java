package com.yourname.smartpantry.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a dish with a name, preparation steps,
 * and the list of ingredients it REQUIRES.
 *
 * The nested Ingredient class mirrors "recipe_ingredients".
 */
public class Recipe {

    private int id;
    private String name;
    private String steps;
    private List<Ingredient> ingredients;

    public Recipe(int id, String name, String steps, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.ingredients = ingredients == null
                ? new ArrayList<>()
                : ingredients;
    }

    public int getId()                        { return id; }
    public String getName()                   { return name; }
    public String getSteps()                  { return steps; }
    public List<Ingredient> getIngredients()  { return ingredients; }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * One required ingredient for a recipe.
     * Kept as a nested static class for simplicity — it is only ever
     * meaningful in the context of a Recipe.
     */
    public static class Ingredient {
        public String name;
        public double quantity;
        public String unit;

        public Ingredient(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    @Override
    public String toString() {
        return name + " (" + ingredients.size() + " ingredients)";
    }
}