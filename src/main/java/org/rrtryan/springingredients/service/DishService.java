package org.rrtryan.springingredients.service;

import org.rrtryan.springingredients.dto.DishDTO;
import org.rrtryan.springingredients.dto.IngredientDTO;
import org.rrtryan.springingredients.entity.Dish;
import org.rrtryan.springingredients.entity.DishIngredient;
import org.rrtryan.springingredients.entity.enums.UnitTypeEnum;
import org.rrtryan.springingredients.exception.NotFoundException;
import org.rrtryan.springingredients.mapper.DishMapper;
import org.rrtryan.springingredients.mapper.IngredientMapper;
import org.rrtryan.springingredients.repository.DishRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishService {
    private final DishMapper dishMapper;
    private final IngredientMapper ingredientMapper;
    DishRepository dishRepository;
    public DishService(DishRepository dishRepository, DishMapper dishMapper, IngredientMapper ingredientMapper) {
        this.dishRepository = dishRepository;
        this.dishMapper = dishMapper;
        this.ingredientMapper = ingredientMapper;
    }

    public List<DishDTO> findAllDishes() throws NotFoundException {
        List<Dish> dishes = dishRepository.findAllDish();
        try {
            if (dishes.isEmpty()) {
                throw new NotFoundException("No dishes were found");
            }
            return dishes.stream().map(dishMapper::toDTO).toList();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public DishDTO updateDishIngredients(Integer id, List<IngredientDTO> newIngredients) throws NotFoundException {
        try {
            Dish dishToUpdate = dishRepository.findByDishId(id);
            if  (dishToUpdate == null) {
                throw new NotFoundException("Dish.id={%d} is not found".formatted(id));
            }

            List<DishIngredient> ingredientLink = newIngredients
                    .stream()
                    .map(ingredientMapper::toEntity)
                    .map(ingredient -> new DishIngredient(0, dishToUpdate, ingredient, 0D, UnitTypeEnum.KG))
                    .toList();

            dishToUpdate.setIngredientsLinkList(ingredientLink);

            Dish dish = dishRepository.saveDish(dishToUpdate);
            if (dish == null) {
                throw new NotFoundException("Dish Not Found");
            }
            return dishMapper.toDTO(dish);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
