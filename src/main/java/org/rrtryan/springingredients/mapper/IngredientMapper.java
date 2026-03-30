package org.rrtryan.springingredients.mapper;

import org.rrtryan.springingredients.dto.IngredientDTO;
import org.rrtryan.springingredients.entity.Ingredient;
import org.springframework.stereotype.Component;

@Component
public class IngredientMapper {
    public IngredientDTO toDTO(Ingredient ingredient) {
        return new IngredientDTO(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getPrice(),
                ingredient.getCategory()
        );
    }

    public Ingredient toEntity(IngredientDTO dto) {
        return new Ingredient(
                dto.getId(),
                dto.getName(),
                dto.getPrice(),
                dto.getCategory()
        );
    }
}
