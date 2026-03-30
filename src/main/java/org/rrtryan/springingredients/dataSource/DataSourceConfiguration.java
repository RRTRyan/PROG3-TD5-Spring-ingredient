package org.rrtryan.springingredients.dataSource;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {
    Dotenv dotenv = Dotenv.load();

    @Bean
    public DataSource getDataSource() {
        return new DriverManagerDataSource(dotenv.get("DATABASE_URL"),
                dotenv.get("DATABASE_USERNAME"),
                dotenv.get("DATABASE_PASSWORD"));
    }
}
