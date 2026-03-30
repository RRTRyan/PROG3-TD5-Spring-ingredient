package org.rrtryan.springingredients.validator;

import org.rrtryan.springingredients.dto.IngredientDTO;
import org.rrtryan.springingredients.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;

import java.util.ArrayList;
import java.util.List;

@Component
public class IngredientValidator {
    private final View error;

    IngredientValidator(View error) {
        this.error = error;
    }

    public void validate(IngredientDTO ingredientDTO) throws BadRequestException {
        if (ingredientDTO == null) {
            throw new BadRequestException("IngredientDTO is null");
        }
        List<String> errors = new ArrayList<>();
        if (ingredientDTO.getId() == null) {
            errors.add("IngredientDTO's id is null");
        }
        if (ingredientDTO.getName() == null || ingredientDTO.getName().trim().isEmpty()) {
            errors.add("IngredientDTO's name is null or empty");
        }
        if (ingredientDTO.getCategory() == null) {
            errors.add("IngredientDTO's category is null");
        }
        if (ingredientDTO.getPrice() == null) {
            errors.add("IngredientDTO's price is null");
        }
        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join(", ", errors));
        }
    }
}
