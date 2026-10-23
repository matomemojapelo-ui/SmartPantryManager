
package com.matomemojapelo.smartpantrymanager.model;

import java.util.List;

public class Recipe {

    private int id;
    private String name;
    private String steps;
    private List<Ingredient> ingredients;

    public Recipe(int id, String name, String steps,
                  List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.ingredients = ingredients;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSteps() {
        return steps;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public static class Ingredient {

        private String name;
        private double quantity;
        private String unit;

        public Ingredient(String name, double quantity,
                          String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }

        public String getName() {
            return name;
        }

        public double getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }
    }
}
