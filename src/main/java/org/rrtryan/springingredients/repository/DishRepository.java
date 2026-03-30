package org.rrtryan.springingredients.repository;

import org.rrtryan.springingredients.entity.*;
import org.rrtryan.springingredients.entity.enums.CategoryEnum;
import org.rrtryan.springingredients.entity.enums.DishTypeEnum;
import org.rrtryan.springingredients.entity.enums.UnitTypeEnum;
import org.rrtryan.springingredients.utils.DishRepositoryUtils;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DishRepository extends GenericRepository {
    private final DishRepositoryUtils dishRepositoryUtils;

    public DishRepository(DishRepositoryUtils dishRepositoryUtils) {
        super();
        this.dishRepositoryUtils = dishRepositoryUtils;
    }

    public Dish findByDishId(Integer id) {
        Connection connection = this.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            SELECT d.id AS dishId, d.name AS dishName, dish_type AS dishType, d.price AS dishPrice,
                                    i.id AS ingId, i.name AS ingName, i.price AS price, i.category AS category,
                                    di.id AS dishIngredientId, quantity_required, unit
                            FROM dish AS d
                            LEFT JOIN dishingredient AS di ON id_dish = d.id
                            LEFT JOIN ingredient AS i ON id_ingredient = i.id WHERE d.id = ?
                            """);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            Dish dish = null;
            while (resultSet.next()) {
                if (resultSet.isFirst()) {
                    int dishId = resultSet.getInt("dishId");
                    String dishName = resultSet.getString("dishName");
                    DishTypeEnum dishType = DishTypeEnum.valueOf(resultSet.getString("dishType"));
                    dish = new Dish(dishId, dishName, dishType, new ArrayList<>());
                    if (resultSet.getObject("dishPrice") == null) {
                        dish.setPrice(null);
                    } else {
                        dish.setPrice(resultSet.getDouble("dishPrice"));
                    }
                }

                if (resultSet.getObject("ingId") != null && dish != null) {
                    int ingredientId = resultSet.getInt("ingId");
                    String ingredientName = resultSet.getString("ingName");
                    double price = resultSet.getDouble("price");
                    CategoryEnum category = CategoryEnum.valueOf(resultSet.getString("category"));

                    Ingredient newIngredient = new Ingredient(ingredientId, ingredientName, price, category);

                    int dishIngredientId = resultSet.getInt("dishIngredientId");
                    Double quantity = resultSet.getDouble("quantity_required");
                    UnitTypeEnum unitType = UnitTypeEnum.valueOf(resultSet.getString("unit"));
                    new DishIngredient(dishIngredientId, dish, newIngredient, quantity, unitType);
                }
            }
            return dish;
        } catch (SQLException | RuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            this.closeConnection(connection);
        }
    }

    public List<Dish> findAllDish() {
        Connection connection = this.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            SELECT DISTINCT d.id AS dishId, d.name AS dishName, d.dish_type AS dishType, d.price AS dishPrice
                            FROM dish AS d
                            LEFT JOIN dishingredient ON d.id = id_dish
                            LEFT JOIN ingredient ON ingredient.id = id_ingredient
                            """);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Dish> dishes = new ArrayList<>();
            while (resultSet.next()) {
                dishes.add(findByDishId(resultSet.getInt("dishId")));
            }
            return dishes;
        } catch (SQLException | RuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            this.closeConnection(connection);
        }
    }

    public List<Dish> findDishsByIngredientName(String ingredientName) {
        Connection connection = this.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            SELECT DISTINCT d.id AS dishId, d.name AS dishName, d.dish_type AS dishType, d.price AS dishPrice
                            FROM dish AS d
                            JOIN dishingredient ON d.id = id_dish
                            JOIN ingredient ON ingredient.id = id_ingredient
                            WHERE ingredient.name ILIKE ?
                            """);
            preparedStatement.setString(1, '%' + ingredientName + '%');
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Dish> dishes = new ArrayList<>();
            while (resultSet.next()) {
                dishes.add(findByDishId(resultSet.getInt("dishId")));
            }
            return dishes;
        } catch (SQLException | RuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            this.closeConnection(connection);
        }
    }

    public Dish saveDish(Dish dishToSave) {
        Connection connection = this.getConnection();
        try {
            connection.setAutoCommit(false);

            String linkDelete = "DELETE FROM dishingredient WHERE id_dish = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(linkDelete);
            preparedStatement.setInt(1, dishToSave.getId());
            preparedStatement.executeUpdate();

            if (!dishToSave.getIngredientsLinkList().isEmpty()) {
                PreparedStatement ingredientInsertStmt = connection.prepareStatement(
                        "INSERT INTO ingredient(id, name, price, category) VALUES " +
                                "(?, ?, ?, CAST(? AS category_enum)), ".repeat(dishToSave.getIngredientsLinkList().size() - 1) +
                                "(?, ?, ?, CAST(? AS category_enum)) ON CONFLICT (id) DO NOTHING"
                );

                int i = 1;
                for (DishIngredient ingredientLink : dishToSave.getIngredientsLinkList()) {
                    ingredientInsertStmt.setInt(i++, ingredientLink.getIngredient().getId());
                    ingredientInsertStmt.setString(i++, ingredientLink.getIngredient().getName());
                    ingredientInsertStmt.setDouble(i++, ingredientLink.getIngredient().getPrice());
                    ingredientInsertStmt.setString(i++, ingredientLink.getIngredient().getCategory().toString());
                }
                ingredientInsertStmt.executeUpdate();
            }

            PreparedStatement dishUpdateStatement = connection.prepareStatement(
                    """
                            INSERT INTO dish (id, name, dish_type, price) VALUES
                            (?, ?, ?::dish_type_enum, ?)
                            ON CONFLICT (id) DO UPDATE
                            SET name = EXCLUDED.name, dish_type = EXCLUDED.dish_type,price = EXCLUDED.price
                            RETURNING id
                            """);
            dishUpdateStatement.setInt(1, dishToSave.getId());
            dishUpdateStatement.setString(2, dishToSave.getName());
            dishUpdateStatement.setString(3, dishToSave.getDishType().toString());
            if (dishToSave.getPrice() != null) {
                dishUpdateStatement.setDouble(4, dishToSave.getPrice());
            } else {
                dishUpdateStatement.setNull(4, Types.DOUBLE);
            }
            dishUpdateStatement.executeQuery();

            dishRepositoryUtils.attachIngredient(connection, dishToSave);

            connection.commit();
            return findByDishId(dishToSave.getId());
        } catch (SQLException | RuntimeException e) {
            this.rollback(connection);
            throw new RuntimeException(e);
        } finally {
            this.closeConnection(connection);
        }
    }

    public Double getDishCost(Integer id) {
        Connection connection = this.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT SUM(i.price * di.quantity_required) FROM ingredient AS i JOIN dishingredient AS di ON di.id_ingredient = i.id WHERE di.id_dish = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException | RuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            this.closeConnection(connection);
        }
        return null;
    }

    public Double getGrossMargin(Integer id) {
        Connection connection = this.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    SELECT ((SELECT dish.price FROM dish WHERE dish.id = ?) - (SELECT SUM((i.price * di.quantity_required)) FROM ingredient AS i JOIN dishingredient AS di ON di.id_ingredient = i.id WHERE di.id_dish = ?))
                    """);
            ps.setInt(1, id);
            ps.setInt(2, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException | RuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            this.closeConnection(connection);
        }
        return null;
    }
}
