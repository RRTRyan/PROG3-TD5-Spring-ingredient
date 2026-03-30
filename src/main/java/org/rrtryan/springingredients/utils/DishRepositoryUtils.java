package org.rrtryan.springingredients.utils;

import org.rrtryan.springingredients.entity.Dish;
import org.rrtryan.springingredients.entity.DishIngredient;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class DishRepositoryUtils extends GenericRepositoryUtils {
    public DishRepositoryUtils() {
        super();
    }

    public void attachIngredient(Connection connection, Dish dish) throws SQLException {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                            INSERT INTO dishingredient (id, id_dish, id_ingredient, quantity_required, unit) VALUES
                            (?, ?, ?, ?, ?::unit_type) ON CONFLICT (id) DO NOTHING
                    """);

            for (DishIngredient ingredientLink : dish.getIngredientsLinkList()) {
                preparedStatement.setInt(1, getNextSequenceValue(connection,
                        "dishingredient",
                        "dishingredient_id_seq"));
                preparedStatement.setInt(2, dish.getId());
                preparedStatement.setInt(3, ingredientLink.getIngredient().getId());
                if (ingredientLink.getQuantityRequired() == null || ingredientLink.getUnit() == null) {
                    throw new SQLException("Ingredient quantity or unit is null");
                }
                preparedStatement.setDouble(4, ingredientLink.getQuantityRequired());
                preparedStatement.setString(5, ingredientLink.getUnit().toString());
                preparedStatement.execute();
            }
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw new RuntimeException(e);
        }
    }
}
