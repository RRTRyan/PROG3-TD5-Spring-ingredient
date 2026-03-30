package org.rrtryan.springingredients.utils;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GenericRepositoryUtils {
    public GenericRepositoryUtils(){}
    public int getNextSequenceValue(Connection connection, String tableName, String sequenceName) throws SQLException {
        try {
            connection.createStatement().execute("SELECT setval('%s', (SELECT MAX(id) FROM %s))".formatted(sequenceName, tableName));
            ResultSet rs = connection.createStatement().executeQuery("SELECT nextval('%s')".formatted(sequenceName));
            rs.next();
            return rs.getInt(1);
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw new RuntimeException(e);
        }
    }
}
