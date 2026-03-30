package org.rrtryan.springingredients.validator;

import org.rrtryan.springingredients.entity.Ingredient;
import org.rrtryan.springingredients.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;

import java.util.ArrayList;
import java.util.List;

@Component
public class IngredientValidator {
    IngredientValidator() {}

    public void validate(Ingredient ingredient) throws BadRequestException {
        if (ingredient == null) {
            throw new BadRequestException("Ingredient is null");
        }
        List<String> errors = new ArrayList<>();
        if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) {
            errors.add("Ingredient's name is null or empty");
        }
        if (ingredient.getCategory() == null) {
            errors.add("Ingredient's category is null");
        }
        if (ingredient.getPrice() == null) {
            errors.add("Ingredient's price is null");
        }
        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join(", ", errors));
        }
    }
}
