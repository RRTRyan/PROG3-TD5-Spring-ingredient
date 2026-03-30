package org.rrtryan.springingredients.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.rrtryan.springingredients.entity.enums.CategoryEnum;
import org.rrtryan.springingredients.entity.enums.MovementTypeEnum;
import org.rrtryan.springingredients.entity.enums.UnitTypeEnum;
import org.rrtryan.springingredients.utils.UnitConversion;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Ingredient {
    private int id;
    private String name;
    private double price;
    private CategoryEnum category;
    private List<StockMovement> stockMovementList;

    public Ingredient(int id, String name, double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stockMovementList = List.of();
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    @JsonIgnore
    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Ingredient that)) return false;
        return id == that.id && Double.compare(price, that.price) == 0 && Objects.equals(name, that.name) && category == that.category && Objects.equals(stockMovementList, that.stockMovementList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, category, stockMovementList);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                '}';
    }

    public StockValue getStockValueAt(Instant t) {
        if (this.stockMovementList.isEmpty()) {
            return new StockValue(0D, UnitTypeEnum.KG);
        }
        this.getStockMovementList().forEach(stockMovement -> {
            UnitConversion.convertToKG(stockMovement.getValue(), this.getName());
        });
        double quantity = this.getStockMovementList().stream()
                .filter(stock -> stock.getCreationDateTime().isBefore(t))
                .mapToDouble(stockMovement -> switch (stockMovement.getType()) {
                    case IN -> stockMovement.getValue().getQuantity();
                    case OUT -> -stockMovement.getValue().getQuantity();
                }).sum();
        UnitTypeEnum unit = (this.stockMovementList.getFirst().getValue().getUnit() != null) ? this.stockMovementList.getFirst().getValue().getUnit() : null;
        return new StockValue(quantity, unit);

    }
}
