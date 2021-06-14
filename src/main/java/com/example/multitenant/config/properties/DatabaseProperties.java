package com.example.multitenant.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class DatabaseProperties {

    @Value("${spring.jpa.properties.hibernate.dialect}")
    private String dialect;

    @Value("${spring.jpa.properties.hibernate.hbm2ddl.auto}")
    private String ddl;

    @Value("${spring.datasource.name}")
    private String directory;

    private Database database;

    @Autowired
    public void setDatabase(Database database) {
        this.database = database;
    }

    public String getDialect() {
        return dialect;
    }

    public String getDdl() {
        return ddl;
    }

    public String getDirectory() {
        return directory;
    }

    public String getUrl() { return database.getUrl(); }

    public String getDriverClassName() { return database.getDriverClassName(); }

    public String getUsername() { return database.getUsername(); }

    public String getPassword() { return database.getUsername(); }

    /**
     * Generates data source.
     *
     * @param identifier of data source.
     * @param defaultIdentifier of default data source.
     * @return newly created data source incl. driver, url, username and password.
     */
    public DataSource dataSource(String identifier, String defaultIdentifier) {
        DataSource dataSource = DataSourceBuilder.create()
                .driverClassName(database.driverClassName)
                .url(database.url.replace("/" + defaultIdentifier, "/" + identifier))
                .username(database.username)
                .password(database.password)
                .build();

        try {
            if (!identifier.equals(defaultIdentifier)) dataSource.getConnection();
        } catch (SQLException ignored) {}

        return dataSource;
    }

    @Bean
    @ConfigurationProperties("spring.datasource")
    protected Database database() { return new Database(); }

    @Getter @Setter
    protected static final class Database {

        String url;
        String driverClassName;
        String username;
        String password;
    }
}
