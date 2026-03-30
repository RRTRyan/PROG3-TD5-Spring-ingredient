package org.rrtryan.springingredients.service;

import org.rrtryan.springingredients.dto.IngredientDTO;
import org.rrtryan.springingredients.entity.Ingredient;
import org.rrtryan.springingredients.entity.StockValue;
import org.rrtryan.springingredients.exception.NotFoundException;
import org.rrtryan.springingredients.mapper.IngredientMapper;
import org.rrtryan.springingredients.repository.IngredientRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public IngredientService(IngredientRepository ingredientRepository,
                             IngredientMapper ingredientMapper) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientMapper = ingredientMapper;
    }

    public List<IngredientDTO> findIngredients() {
        try {
            List<Ingredient> ingredients = ingredientRepository.findIngredients(1, 100);
            return ingredients.stream().map(ingredientMapper::toDTO).toList();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public IngredientDTO findIngredientById(Integer id) throws NotFoundException {
        try {
            Ingredient ingredients = ingredientRepository.findIngredientById(id);
            if (ingredients == null) {
                throw new NotFoundException("Ingredient.id={%d} not found".formatted(id));
            }
            return ingredientMapper.toDTO(ingredients);
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
}
