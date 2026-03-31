package org.rrtryan.springingredients.utils;

import org.rrtryan.springingredients.entity.*;
import org.rrtryan.springingredients.entity.enums.CategoryEnum;
import org.rrtryan.springingredients.entity.enums.MovementTypeEnum;
import org.rrtryan.springingredients.entity.enums.UnitTypeEnum;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class IngredientRepositoryUtils extends GenericRepositoryUtils{
    public IngredientRepositoryUtils() {
        super();
    }

    public void createIngredientList(Connection connection, ResultSet resultSet, List<Ingredient> ingredients) {
        try {
            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient(
                        resultSet.getInt("ingId"),
                        resultSet.getString("ingName"),
                        resultSet.getDouble("price"),
                        CategoryEnum.valueOf(resultSet.getString("category"))
                );
                ingredient.setStockMovementList(getIngredientStockMovementList(connection, ingredient.getId()));
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<StockMovement> getIngredientStockMovementList(Connection connection, Integer ingredientId) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT id, quantity, unit, type, creation_datetime FROM stock_movement WHERE id_ingredient = ?");
            ps.setInt(1, ingredientId);
            ResultSet rs = ps.executeQuery();
            List<StockMovement> stockMovementList = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                double quantity = rs.getDouble("quantity");
                UnitTypeEnum unit = UnitTypeEnum.valueOf(rs.getString("unit"));
                MovementTypeEnum movementType = MovementTypeEnum.valueOf(rs.getString("type"));
                Instant creationDateTime = rs.getTimestamp("creation_datetime").toInstant();
                stockMovementList.add(new StockMovement(id, new StockValue(quantity, unit), movementType, creationDateTime));
            }
            return stockMovementList;
        } catch (RuntimeException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createStockMovementRecord(Connection connection, Ingredient ingredient) throws SQLException {
        try {
            PreparedStatement ps = connection.prepareStatement(("INSERT INTO stock_movement (id, id_ingredient, quantity, unit, type, creation_datetime) VALUES " +
                    "(?, ?, ?, ?::unit_type, ?::stock_movement_enum, ?), ".repeat(Math.max(((ingredient.getStockMovementList().size()) - 1), 0)) +
                    "(?, ?, ?, ?::unit_type, ?::stock_movement_enum, ?) ON CONFLICT (id) DO NOTHING"));
            int i = 1;
            int j = getNextSequenceValue(connection, "stock_movement", "stock_movement_id_seq");
            for (StockMovement stockMovement : ingredient.getStockMovementList()) {
                ps.setInt(i++, j++);
                ps.setInt(i++, ingredient.getId());
                ps.setDouble(i++, stockMovement.getValue().getQuantity());
                ps.setString(i++, stockMovement.getValue().getUnit().toString());
                ps.setString(i++, stockMovement.getType().toString());
                ps.setTimestamp(i++, Timestamp.from(stockMovement.getCreationDateTime()));
            }
            ps.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
