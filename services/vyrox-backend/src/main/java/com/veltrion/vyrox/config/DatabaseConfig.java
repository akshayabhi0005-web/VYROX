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

        String inputUrl = envDbUrl != null ? envDbUrl.trim() : "";
        String finalUsername = envUsername != null ? envUsername.trim() : "";
        String finalPassword = envPassword != null ? envPassword.trim() : "";

        String targetJdbcUrl = inputUrl;
        String host = "localhost";
        int port = 5432;
        String dbName = "postgres";

        // Check if URI-style connection string (postgres:// or postgresql://)
        if (inputUrl.startsWith("postgres://") || inputUrl.startsWith("postgresql://")) {
            try {
                URI uri = new URI(inputUrl);
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

                host = uri.getHost() != null ? uri.getHost() : "aws-0-ap-northeast-1.pooler.supabase.com";
                port = uri.getPort() > 0 ? uri.getPort() : 5432;
                dbName = uri.getPath() != null && !uri.getPath().isEmpty() ? uri.getPath().replaceAll("^/", "") : "postgres";

                // Automatically redirect any direct Supabase host (db.*.supabase.co) to Session Pooler to avoid IPv6 issues
                if (host.startsWith("db.") && host.endsWith(".supabase.co")) {
                    logger.info("Direct Supabase hostname detected ({}). Redirecting to Supabase Session Pooler.", host);
                    host = "aws-0-ap-northeast-1.pooler.supabase.com";
                    port = 5432;
                }

                String query = uri.getQuery() != null ? uri.getQuery() : "sslmode=require";
                if (!query.contains("sslmode")) {
                    query += (query.isEmpty() ? "" : "&") + "sslmode=require";
                }

                targetJdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?" + query;
            } catch (Exception e) {
                logger.warn("Could not parse URI format for DATABASE_URL; falling back to direct JDBC string", e);
                targetJdbcUrl = "jdbc:" + inputUrl;
            }
        } else if (inputUrl.startsWith("jdbc:postgresql://")) {
            // Already standard JDBC format: parse host/port for logging and direct-host auto-fix
            try {
                String uriPart = inputUrl.substring("jdbc:".length());
                URI uri = new URI(uriPart);
                host = uri.getHost() != null ? uri.getHost() : "aws-0-ap-northeast-1.pooler.supabase.com";
                port = uri.getPort() > 0 ? uri.getPort() : 5432;
                dbName = uri.getPath() != null && !uri.getPath().isEmpty() ? uri.getPath().replaceAll("^/", "") : "postgres";

                if (host.startsWith("db.") && host.endsWith(".supabase.co")) {
                    logger.info("Direct Supabase hostname detected ({}). Redirecting to Supabase Session Pooler.", host);
                    host = "aws-0-ap-northeast-1.pooler.supabase.com";
                    port = 5432;
                    String query = uri.getQuery() != null ? uri.getQuery() : "sslmode=require";
                    if (!query.contains("sslmode")) {
                        query += (query.isEmpty() ? "" : "&") + "sslmode=require";
                    }
                    targetJdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?" + query;
                }
            } catch (Exception e) {
                // Ignore parse errors on raw JDBC string
            }
        }

        // Determine driver class
        String driverClass = rawDriver != null ? rawDriver.trim() : "";
        if (driverClass.isEmpty()) {
            if (targetJdbcUrl.startsWith("jdbc:postgresql:")) {
                driverClass = "org.postgresql.Driver";
            } else if (targetJdbcUrl.startsWith("jdbc:h2:")) {
                driverClass = "org.h2.Driver";
            } else {
                driverClass = "org.postgresql.Driver";
            }
        }

        // Secure logging (NO credentials logged)
        logger.info("Initializing VYROX DataSource. Target Host: {}, Port: {}, Database: {}, Driver: {}, User: {}", 
                host, port, dbName, driverClass, finalUsername);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(targetJdbcUrl);
        config.setDriverClassName(driverClass);
        if (!finalUsername.isEmpty()) {
            config.setUsername(finalUsername);
        }
        if (!finalPassword.isEmpty()) {
            config.setPassword(finalPassword);
        }

        // Production connection pool tuning for Supabase Session Pooler
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30s timeout
        config.setIdleTimeout(300000);      // 5 min
        config.setMaxLifetime(600000);      // 10 min
        config.setValidationTimeout(5000);  // 5s
        config.setPoolName("VyroxHikariPool");

        if (targetJdbcUrl.startsWith("jdbc:postgresql:")) {
            config.addDataSourceProperty("tcpKeepAlive", "true");
            config.addDataSourceProperty("connectTimeout", "30");
            config.addDataSourceProperty("socketTimeout", "60");
            config.addDataSourceProperty("sslmode", "require");
        }

        return new HikariDataSource(config);
    }
}
