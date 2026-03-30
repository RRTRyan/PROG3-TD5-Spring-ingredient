package org.rrtryan.springingredients.dto;

import org.rrtryan.springingredients.entity.Ingredient;
import org.rrtryan.springingredients.entity.enums.CategoryEnum;
import org.springframework.stereotype.Component;

public class IngredientDTO {
    private final Integer id;
    private final String name;
    private final Double price;
    private final CategoryEnum category;

    public IngredientDTO(Integer id, String name, Double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
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

    public CategoryEnum getCategory() {
        return category;
    }
}
