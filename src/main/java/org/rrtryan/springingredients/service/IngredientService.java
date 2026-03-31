package org.rrtryan.springingredients.service;

import org.rrtryan.springingredients.entity.Ingredient;
import org.rrtryan.springingredients.entity.Movement;
import org.rrtryan.springingredients.entity.StockMovement;
import org.rrtryan.springingredients.entity.StockValue;
import org.rrtryan.springingredients.exception.NotFoundException;
import org.rrtryan.springingredients.repository.IngredientRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public List<Ingredient> findIngredients() {
        try {
             return ingredientRepository.findIngredients(0, Integer.MAX_VALUE);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient findIngredientById(Integer id) throws NotFoundException {
        try {
            Ingredient ingredients = ingredientRepository.findIngredientById(id);
            if (ingredients == null) {
                throw new NotFoundException("Ingredient.id={%d} not found".formatted(id));
            }
            return ingredients;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public StockValue getStockValueAt(Instant t, Integer ingId) throws  NotFoundException {
        try {
            return ingredientRepository.findIngredientById(ingId).getStockValueAt(t);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    };

    public List<StockMovement> addStockMovement(Integer id, List<Movement> movements) throws NotFoundException {
        try {
            Ingredient ingredient = findIngredientById(id);
            List<StockMovement> stockMovementList = new ArrayList<>();
            Instant ingredientCreationDateTime = Instant.now();
            for  (Movement movement : movements) {
                stockMovementList.add(
                        new StockMovement(
                                1,
                                new StockValue(movement.getQuantity(), movement.getUnit()),
                                movement.getType(),
                                ingredientCreationDateTime
                        ));
            };
            ingredient.setStockMovementList(stockMovementList);
            return ingredientRepository.updateStockMovement(ingredient).stream()
                    .filter(stockMovement -> stockMovement.getCreationDateTime().isAfter(ingredientCreationDateTime.minusSeconds(1)))
                    .toList();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
