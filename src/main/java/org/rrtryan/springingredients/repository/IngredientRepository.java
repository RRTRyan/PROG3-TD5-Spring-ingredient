package org.rrtryan.springingredients.repository;

import org.rrtryan.springingredients.entity.StockMovement;
import org.rrtryan.springingredients.entity.enums.CategoryEnum;
import org.rrtryan.springingredients.entity.Ingredient;

import org.rrtryan.springingredients.utils.IngredientRepositoryUtils;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class IngredientRepository extends GenericRepository {
    private final IngredientRepositoryUtils ingredientRepositoryUtils;

    public IngredientRepository(IngredientRepositoryUtils ingredientRepositoryUtils) {
        super();
        this.ingredientRepositoryUtils = ingredientRepositoryUtils;
    }

    public List<Ingredient> findIngredients(int page, int size) {
        Connection connection = getConnection();
        try {
            boolean getAllIngredients = (size == Integer.MAX_VALUE && page == 0);
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            SELECT i.id AS ingId, i.name AS ingName, i.price, i.category
                            FROM ingredient AS i
                            %s
                            """.formatted(getAllIngredients ? "" : "LIMIT ? OFFSET ?")
            );
            if (!getAllIngredients) {
                preparedStatement.setInt(1, (page - 1) * size);
                preparedStatement.setInt(2, size);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Ingredient> ingredients = new ArrayList<>();
            ingredientRepositoryUtils.createIngredientList(connection, resultSet, ingredients);
            preparedStatement.close();
            return ingredients;
        } catch (SQLException | RuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    public Ingredient findIngredientById(Integer id) {
        Connection connection = getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            SELECT i.id AS ingId, i.name AS ingName, i.price, i.category
                            FROM ingredient AS i
                            WHERE i.id = ?
                    """
            );
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            List<Ingredient> ingredients = new ArrayList<>();
            ingredientRepositoryUtils.createIngredientList(connection, rs,  ingredients);
            if (ingredients.isEmpty()) {
                return null;
            }
            return ingredients.getFirst();
        } catch (SQLException | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);

            if (newIngredients.isEmpty()) {
                return newIngredients;
            }

            PreparedStatement ingredientInsertStmt = connection.prepareStatement(
                    "INSERT INTO ingredient VALUES " +
                            "(?, ?, ?, CAST(? AS category_enum)), ".repeat(newIngredients.size() - 1) +
                            "(?, ?, ?, CAST(? AS category_enum)) ON CONFLICT (id) DO NOTHING"
            );

            int i = 1;
            for (Ingredient ingredient : newIngredients) {
                ingredientInsertStmt.setInt(i++, ingredient.getId());
                ingredientInsertStmt.setString(i++, ingredient.getName());
                ingredientInsertStmt.setDouble(i++, ingredient.getPrice());
                ingredientInsertStmt.setString(i++, ingredient.getCategory().toString());
            }
            ingredientInsertStmt.executeUpdate();
            connection.commit();

            StringBuilder sql = new StringBuilder(
                    """
                            SELECT i.id as ingId, i.name as ingName, i.price, i.category,
                            d.id as dishId, d.name as dishName, dish_type as dishType
                            FROM ingredient AS i
                            LEFT JOIN dishingredient ON i.id = id_ingredient
                            LEFT JOIN dish AS d ON d.id = id_dish
                            WHERE 1 = 1
                            """
            );

            for (int j = 0; j < newIngredients.size(); j++) {
                if (j == 0) {
                    sql.append(" AND i.id = ?");
                } else sql.append(" OR i.id = ?");
            }

            PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
            i = 1;
            for (Ingredient ingredient : newIngredients) {
                preparedStatement.setInt(i++, ingredient.getId());
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            List<Ingredient> ingredients = new ArrayList<>();
            ingredientRepositoryUtils.createIngredientList(connection, resultSet, ingredients);
            return ingredients;
        } catch (SQLException | RuntimeException e) {
            rollback(connection);
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName,
                                                      CategoryEnum category,
                                                      String dishName,
                                                      int page,
                                                      int size) throws SQLException {
        Connection connection = getConnection();
        try {
            StringBuilder query = new StringBuilder(
                    """
                            SELECT i.id AS ingId, i.name AS ingName, i.price AS price, i.category AS category,
                            id_dish AS dishId, d.name AS dishName, dish_type AS dishType
                            FROM dish AS d
                            JOIN dishingredient ON d.id = id_dish
                            RIGHT JOIN ingredient AS i ON i.id = id_ingredient
                            """);
            List<String> conditions = new ArrayList<>();
            if (ingredientName != null || category != null || dishName != null) {
                query.append(" WHERE ");
                if (ingredientName != null) {
                    conditions.add(" i.name ILIKE ? ");
                }
                if (category != null) {
                    conditions.add(" i.category = CAST(? AS category_enum) ");
                }
                if (dishName != null) {
                    conditions.add(" d.name ILIKE ? ");
                }
                query.append(conditions.stream().reduce((a, b) -> a + " AND " + b).get());
            }
            query.append(" LIMIT ? OFFSET ?");
            PreparedStatement preparedStatement = connection.prepareStatement(query.toString());
            int i = 1;
            if (ingredientName != null) preparedStatement.setString(i++, '%' + ingredientName + '%');
            if (category != null) preparedStatement.setString(i++, category.toString());
            if (dishName != null) preparedStatement.setString(i++, '%' + dishName + '%');
            preparedStatement.setInt(i++, (size > 0 ? size : 10));
            preparedStatement.setInt(i, (page > 0) ? ((page - 1) * size) : 1);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Ingredient> ingredients = new ArrayList<>();
            ingredientRepositoryUtils.createIngredientList(connection, resultSet, ingredients);
            return ingredients;
        } catch (SQLException | RuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    public Ingredient saveIngredient(Ingredient toSave) {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            createIngredients(List.of(toSave));
            ingredientRepositoryUtils.createStockMovementRecord(connection, toSave);
            connection.commit();
            Ingredient ingredient = findIngredientsByCriteria(toSave.getName(), toSave.getCategory(), null, 1, 1).getLast();
            ingredient.setStockMovementList(ingredientRepositoryUtils.getIngredientStockMovementList(connection, toSave.getId()));
            return ingredient;
        } catch (SQLException | RuntimeException e) {
            rollback(connection);
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }

    public List<StockMovement> updateStockMovement(Ingredient ingredient) {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            ingredientRepositoryUtils.createStockMovementRecord(connection, ingredient);
            connection.commit();
            ingredient.setStockMovementList(ingredientRepositoryUtils.getIngredientStockMovementList(connection, ingredient.getId()));
            return ingredient.getStockMovementList();
        } catch (SQLException | RuntimeException e) {
            rollback(connection);
            throw new RuntimeException(e);
        } finally {
            closeConnection(connection);
        }
    }
}
