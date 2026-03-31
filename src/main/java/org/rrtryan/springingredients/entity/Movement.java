package org.rrtryan.springingredients.entity;

import org.rrtryan.springingredients.entity.enums.MovementTypeEnum;
import org.rrtryan.springingredients.entity.enums.UnitTypeEnum;

import java.util.Objects;

public class Movement {
    private UnitTypeEnum unit;
    private Double quantity;
    private MovementTypeEnum type;

    public Movement(UnitTypeEnum unit, Double quantity, MovementTypeEnum type) {
        this.unit = unit;
        this.quantity = quantity;
        this.type = type;
    }

    public UnitTypeEnum getUnit() {
        return unit;
    }

    public void setUnit(UnitTypeEnum unit) {
        this.unit = unit;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public MovementTypeEnum getType() {
        return type;
    }

    public void setType(MovementTypeEnum type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Movement movement)) return false;
        return unit == movement.unit && Objects.equals(quantity, movement.quantity) && type == movement.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit, quantity, type);
    }

    @Override
    public String toString() {
        return "Movement{" +
                "unit=" + unit +
                ", quantity=" + quantity +
                ", type=" + type +
                '}';
    }
}
