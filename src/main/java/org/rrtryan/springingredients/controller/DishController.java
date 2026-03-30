package org.rrtryan.springingredients.controller;

import org.rrtryan.springingredients.entity.Ingredient;
import org.rrtryan.springingredients.exception.BadRequestException;
import org.rrtryan.springingredients.exception.NotFoundException;
import org.rrtryan.springingredients.service.DishService;
import org.rrtryan.springingredients.validator.IngredientValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dishes")
public class DishController {
    DishService dishService;
    IngredientValidator ingredientValidator;
    public DishController(DishService dishService, IngredientValidator ingredientValidator) {
        this.dishService = dishService;
        this.ingredientValidator = ingredientValidator;
    }

    @GetMapping
    public ResponseEntity<?> getDishes() {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(dishService.findAllDishes());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (HttpMessageNotWritableException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/ingredients")
    public ResponseEntity<?> updateDishIngredients(@PathVariable(required = false) Integer id, @RequestBody(required = false) List<Ingredient> ingredients) {
        try {
            if (id == null) {
                throw new BadRequestException("Missing id");
            }
            if (ingredients == null) {
                throw new BadRequestException("Missing ingredients");
            }
            for (Ingredient ingredient : ingredients) {
                ingredientValidator.validate(ingredient);
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(dishService.updateDishIngredients(id, ingredients));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
