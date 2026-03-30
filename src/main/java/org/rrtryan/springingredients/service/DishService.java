package org.rrtryan.springingredients.service;

import org.rrtryan.springingredients.entity.Dish;
import org.rrtryan.springingredients.entity.DishIngredient;
import org.rrtryan.springingredients.entity.Ingredient;
import org.rrtryan.springingredients.entity.enums.UnitTypeEnum;
import org.rrtryan.springingredients.exception.NotFoundException;
import org.rrtryan.springingredients.repository.DishRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishService {
    DishRepository dishRepository;
    public DishService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    public List<Dish> findAllDishes() throws NotFoundException {
        List<Dish> dishes = dishRepository.findAllDish();
        try {
            if (dishes.isEmpty()) {
                throw new NotFoundException("No dishes were found");
            }
            return dishes;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish updateDishIngredients(Integer id, List<Ingredient> newIngredients) throws NotFoundException {
        try {
            Dish dishToUpdate = dishRepository.findByDishId(id);
            if  (dishToUpdate == null) {
                throw new NotFoundException("Dish.id={%d} is not found".formatted(id));
            }

            List<DishIngredient> ingredientLink = newIngredients
                    .stream()
                    .map(ingredient -> new DishIngredient(0, dishToUpdate, ingredient, 0D, UnitTypeEnum.KG))
                    .toList();

            dishToUpdate.setIngredientsLinkList(ingredientLink);

            Dish dish = dishRepository.saveDish(dishToUpdate);
            if (dish == null) {
                throw new NotFoundException("Dish Not Found");
            }
            return dish;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
