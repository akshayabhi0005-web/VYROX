package com.veltrion.vyrox.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:jdbc:h2:mem:vyroxdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL}")
    private String rawUrl;

    @Value("${spring.datasource.username:sa}")
    private String rawUsername;

    @Value("${spring.datasource.password:}")
    private String rawPassword;

    @Value("${spring.datasource.driver-class-name:}")
    private String rawDriver;

    @Bean
    @Primary
    public DataSource dataSource() {
        String envDbUrl = System.getenv("DATABASE_URL");
        if (envDbUrl == null || envDbUrl.trim().isEmpty()) {
            envDbUrl = System.getenv("DB_URL");
        }
        if (envDbUrl == null || envDbUrl.trim().isEmpty()) {
            envDbUrl = rawUrl;
        }

        String envUsername = System.getenv("DB_USERNAME");
        if (envUsername == null || envUsername.trim().isEmpty()) {
            envUsername = rawUsername;
        }

        String envPassword = System.getenv("DB_PASSWORD");
        if (envPassword == null || envPassword.trim().isEmpty()) {
            envPassword = rawPassword;
        }

        String jdbcUrl = envDbUrl.trim();
        String finalUsername = envUsername != null ? envUsername.trim() : "";
        String finalPassword = envPassword != null ? envPassword.trim() : "";

        // Normalize postgres:// or postgresql:// URIs into standard JDBC format if provided without jdbc: prefix
        if (jdbcUrl.startsWith("postgres://") || jdbcUrl.startsWith("postgresql://")) {
            try {
                URI uri = new URI(jdbcUrl);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] userParts = userInfo.split(":", 2);
                    if (finalUsername.isEmpty() || finalUsername.equals("sa")) {
                        finalUsername = userParts[0];
                    }
                    if (finalPassword.isEmpty()) {
                        finalPassword = userParts[1];
                    }
                }
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath() != null ? uri.getPath() : "/postgres";
                String query = uri.getQuery() != null ? "?" + uri.getQuery() : "?sslmode=require";
                if (!query.contains("sslmode")) {
                    query += (query.startsWith("?") ? "&" : "?") + "sslmode=require";
                }
                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path + query;
            } catch (Exception e) {
                logger.warn("Could not parse URI format for DATABASE_URL, attempting prefix prepend", e);
                jdbcUrl = "jdbc:" + jdbcUrl;
            }
        }

        // Determine driver class
        String driverClass = rawDriver != null ? rawDriver.trim() : "";
        if (driverClass.isEmpty()) {
            if (jdbcUrl.startsWith("jdbc:postgresql:")) {
                driverClass = "org.postgresql.Driver";
            } else if (jdbcUrl.startsWith("jdbc:h2:")) {
                driverClass = "org.h2.Driver";
            } else {
                driverClass = "org.postgresql.Driver";
            }
        }

        // Safe URL logging (mask credentials if any)
        String maskedUrl = jdbcUrl.replaceAll(":[^/@]+@", ":****@");
        logger.info("Configuring VYROX DataSource. Target: {}, Driver: {}, User: {}", 
                maskedUrl, driverClass, finalUsername);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName(driverClass);
        if (!finalUsername.isEmpty()) {
            config.setUsername(finalUsername);
        }
        if (!finalPassword.isEmpty()) {
            config.setPassword(finalPassword);
        }

        // Production-ready connection pool tuning
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30s timeout
        config.setIdleTimeout(300000);      // 5 min
        config.setMaxLifetime(600000);      // 10 min
        config.setValidationTimeout(5000);  // 5s
        config.setPoolName("VyroxHikariPool");

        return new HikariDataSource(config);
    }
}
