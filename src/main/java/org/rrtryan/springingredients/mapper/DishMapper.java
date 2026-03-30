package org.rrtryan.springingredients.mapper;

import org.rrtryan.springingredients.dto.DishDTO;
import org.rrtryan.springingredients.dto.IngredientDTO;
import org.rrtryan.springingredients.entity.Dish;
import org.rrtryan.springingredients.entity.DishIngredient;
import org.rrtryan.springingredients.entity.Ingredient;
import org.rrtryan.springingredients.entity.enums.CategoryEnum;
import org.rrtryan.springingredients.entity.enums.UnitTypeEnum;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DishMapper {
    IngredientMapper ingredientMapper;
    DishMapper(IngredientMapper ingredientMapper) {
        this.ingredientMapper = ingredientMapper;
    }

    public DishDTO toDTO(Dish dish) {
        List<IngredientDTO> ingredients = dish.getIngredientsLinkList()
                .stream()
                .map(DishIngredient::getIngredient)
                .map(ingredientMapper::toDTO)
                .toList();

        return new DishDTO(
                dish.getId(),
                dish.getName(),
                dish.getPrice(),
                ingredients);
    }

    public Dish toEntity(DishDTO dishDTO) {
        return new Dish(
                dishDTO.getId(),
                dishDTO.getName(),
                null,
                dishDTO.getIngredient()
                        .stream()
                        .map(ingredientMapper::toEntity)
                        .map(ingredient -> new DishIngredient(
                                0,
                                null,
                                ingredient,
                                null,
                                UnitTypeEnum.KG
                        )).toList()
        );
    };
}
