package org.rrtryan.springingredients.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.rrtryan.springingredients.entity.enums.DishTypeEnum;

import java.util.List;
import java.util.Objects;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private List<DishIngredient> ingredientsLinkList;
    private Double price;

    public Dish(int id, String name, DishTypeEnum dishType, List<DishIngredient> ingredientsLink) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredientsLinkList = ingredientsLink;
        this.price = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    @JsonIgnore
    public List<DishIngredient> getIngredientsLinkList() {
        return ingredientsLinkList;
    }

    public void setIngredientsLinkList(List<DishIngredient> ingredientsLinkList) {
        this.ingredientsLinkList = ingredientsLinkList;
    }

    public List<Ingredient> getIngredients() {
        return this.getIngredientsLinkList()
                .stream()
                .map(DishIngredient::getIngredient)
                .toList();
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", ingredients=" + ingredientsLinkList +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Dish dish)) return false;
        return id == dish.id && Objects.equals(name, dish.name) && dishType == dish.dishType && Objects.equals(ingredientsLinkList, dish.ingredientsLinkList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishType, ingredientsLinkList);
    }

    @JsonIgnore
    public Double getDishCost() {
        for (DishIngredient ingredientLink : ingredientsLinkList) {
            if (ingredientLink.getQuantityRequired() == null) {
                throw new RuntimeException("Ingredient [%s, ID=%d] quantity is null".formatted(ingredientLink.getIngredient().getName(), ingredientLink.getIngredient().getId()));
            }
        }
        return this.ingredientsLinkList.stream().mapToDouble(ingredient -> (ingredient.getIngredient().getPrice() * ingredient.getQuantityRequired())).sum();
    }

    @JsonIgnore
    public Double getGrossMargin() {
        if (this.price == null) {
            throw new RuntimeException("Price is null");
        }
        return this.getPrice() - this.getDishCost();
    }

}
