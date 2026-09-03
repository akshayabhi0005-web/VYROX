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
    private String defaultUrl;

    @Value("${spring.datasource.username:sa}")
    private String defaultUsername;

    @Value("${spring.datasource.password:}")
    private String defaultPassword;

    @Value("${spring.datasource.driver-class-name:}")
    private String defaultDriver;

    @Bean
    @Primary
    public DataSource dataSource() {
        // 1. Resolve environment variables with property fallbacks
        String rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            rawUrl = System.getenv("DB_URL");
        }
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            rawUrl = defaultUrl;
        }
        rawUrl = rawUrl.trim();

        String rawUsername = System.getenv("DB_USERNAME");
        if (rawUsername == null || rawUsername.trim().isEmpty()) {
            rawUsername = defaultUsername;
        }
        rawUsername = rawUsername != null ? rawUsername.trim() : "";

        String rawPassword = System.getenv("DB_PASSWORD");
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = defaultPassword;
        }
        rawPassword = rawPassword != null ? rawPassword.trim() : "";

        String finalJdbcUrl = rawUrl;
        String finalUsername = rawUsername;
        String finalPassword = rawPassword;
        String driverClass = defaultDriver != null ? defaultDriver.trim() : "";

        String logHost = "embedded";
        int logPort = 0;
        String logDbName = "vyroxdb";

        // 2. Process PostgreSQL URLs (both URI and JDBC format)
        if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://") || rawUrl.startsWith("jdbc:postgresql://")) {
            driverClass = "org.postgresql.Driver";

            try {
                String parseTarget = rawUrl.startsWith("jdbc:") ? rawUrl.substring(5) : rawUrl;
                // Normalize scheme for java.net.URI parsing
                if (!parseTarget.startsWith("postgres://") && !parseTarget.startsWith("postgresql://")) {
                    parseTarget = "postgresql://" + parseTarget.replaceFirst("^[a-zA-Z0-9_-]+://", "");
                }

                URI uri = new URI(parseTarget);

                // Extract credentials embedded in URI if not already supplied
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    if (finalUsername.isEmpty() || "sa".equalsIgnoreCase(finalUsername)) {
                        finalUsername = parts[0];
                    }
                    if (finalPassword.isEmpty()) {
                        finalPassword = parts[1];
                    }
                } else if (userInfo != null && !userInfo.isEmpty()) {
                    if (finalUsername.isEmpty() || "sa".equalsIgnoreCase(finalUsername)) {
                        finalUsername = userInfo;
                    }
                }

                String host = uri.getHost() != null ? uri.getHost() : "aws-0-ap-northeast-1.pooler.supabase.com";
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath() != null && !uri.getPath().isEmpty() ? uri.getPath().replaceAll("^/", "") : "postgres";

                // Automatically map old direct host (db.*.supabase.co) to Session Pooler
                if (host.startsWith("db.") && host.endsWith(".supabase.co")) {
                    logger.info("Direct Supabase hostname detected ({}). Redirecting to Session Pooler.", host);
                    host = "aws-0-ap-northeast-1.pooler.supabase.com";
                    port = 5432;
                }

                String query = uri.getQuery() != null ? uri.getQuery() : "";
                if (!query.contains("sslmode")) {
                    query = query.isEmpty() ? "sslmode=require" : query + "&sslmode=require";
                }

                finalJdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + path + "?" + query;
                logHost = host;
                logPort = port;
                logDbName = path;

            } catch (Exception e) {
                logger.warn("Could not parse PostgreSQL URI target; using standard JDBC format", e);
                if (!rawUrl.startsWith("jdbc:")) {
                    finalJdbcUrl = "jdbc:" + rawUrl;
                }
            }
        } else if (rawUrl.startsWith("jdbc:h2:")) {
            driverClass = "org.h2.Driver";
            logHost = "h2-in-memory";
            logPort = 0;
            logDbName = "vyroxdb";
        }

        if (driverClass.isEmpty()) {
            driverClass = finalJdbcUrl.startsWith("jdbc:postgresql:") ? "org.postgresql.Driver" : "org.h2.Driver";
        }

        // 3. Log strictly non-sensitive connection details
        logger.info("Initializing VYROX DataSource. Host: {}, Port: {}, Database: {}, Driver: {}, User: {}",
                logHost, logPort, logDbName, driverClass, finalUsername);

        // 4. Configure single Hikari DataSource
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(finalJdbcUrl);
        config.setDriverClassName(driverClass);

        if (!finalUsername.isEmpty()) {
            config.setUsername(finalUsername);
        }
        if (!finalPassword.isEmpty()) {
            config.setPassword(finalPassword);
        }

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30s connection timeout
        config.setIdleTimeout(300000);      // 5 min
        config.setMaxLifetime(600000);      // 10 min
        config.setValidationTimeout(5000);  // 5s
        config.setPoolName("VyroxHikariPool");

        if (finalJdbcUrl.startsWith("jdbc:postgresql:")) {
            config.addDataSourceProperty("tcpKeepAlive", "true");
            config.addDataSourceProperty("connectTimeout", "30");
            config.addDataSourceProperty("socketTimeout", "60");
            config.addDataSourceProperty("sslmode", "require");
        }

        return new HikariDataSource(config);
    }
}
