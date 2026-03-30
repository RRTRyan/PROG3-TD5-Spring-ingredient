package org.rrtryan.springingredients.dto;

import org.rrtryan.springingredients.entity.Ingredient;
import org.rrtryan.springingredients.entity.enums.CategoryEnum;

import java.util.List;

public class DishDTO {
    private final Integer id;
    private final String name;
    private final Double price;
    private final List<IngredientDTO> ingredients;

    public DishDTO(Integer id, String name, Double price, List<IngredientDTO> ingredients) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.ingredients = ingredients;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public List<IngredientDTO> getIngredient() {
        return ingredients;
    }
}
