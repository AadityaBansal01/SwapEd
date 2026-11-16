package com.swaped.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Configures the MySQL DataSource for swap_ed_db.
 * Attempts primary MySQL connection (localhost:3306) first, then local project MySQL instance (127.0.0.1:3307).
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url}")
    private String primaryUrl;

    @Value("${spring.datasource.FallbackUrl:jdbc:mysql://127.0.0.1:3307/swap_ed_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}")
    private String fallbackMysqlUrl;

    @Value("${spring.datasource.username:root}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        if (canConnect(primaryUrl, username, password)) {
            log.info("Connected to primary MySQL database: {}", primaryUrl);
            return buildHikari(primaryUrl, username, password, "com.mysql.cj.jdbc.Driver");
        }

        if (canConnect(fallbackMysqlUrl, "root", "")) {
            log.info("Connected to project MySQL database (swap_ed_db on 127.0.0.1:3307)");
            return buildHikari(fallbackMysqlUrl, "root", "", "com.mysql.cj.jdbc.Driver");
        }

        String h2MySqlCompatUrl = "jdbc:h2:file:./data/swap_ed_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE";
        log.warn("MySQL server not reachable on 3306 or 3307; using MySQL-compatible local persistence: {}", h2MySqlCompatUrl);
        return buildHikari(h2MySqlCompatUrl, "sa", "", "org.h2.Driver");
    }

    private boolean canConnect(String url, String user, String pass) {
        try {
            DriverManager.setLoginTimeout(2);
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                return conn != null && conn.isValid(2);
            }
        } catch (Exception e) {
            return false;
        }
    }

    private DataSource buildHikari(String url, String user, String pass, String driverClass) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(pass);
        config.setDriverClassName(driverClass);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setPoolName("SwapEdMySqlPool");
        return new HikariDataSource(config);
    }
}
