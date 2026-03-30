package org.rrtryan.springingredients.repository;

import org.rrtryan.springingredients.dataSource.DataSourceConfiguration;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Repository
public class GenericRepository {
    public final DataSource dataSource;
    public GenericRepository() {
        DataSourceConfiguration dataSourceConfiguration = new DataSourceConfiguration();
        this.dataSource = dataSourceConfiguration.getDataSource();
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeConnection(Connection connection) {
        try {
            if  (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback(Connection connection) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
