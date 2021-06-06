package com.example.multitenant.config;

import com.example.multitenant.config.properties.DatabaseProperties;
import com.example.multitenant.config.properties.SecurityProperties;
import com.example.multitenant.exceptionhandler.exceptions.DataSourceException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Configuration
@Profile("!test")
public class DatabaseConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DatabaseProperties databaseProperties;
    private final SecurityProperties securityProperties;
    private final ApplicationContext applicationContext;
    private final ResourceLoader resourceLoader;
    private final Map<Object, Object> configurations;
    private final RoutingDataSource dataSource;

    @Autowired
    public DatabaseConfig(DatabaseProperties databaseProperties, SecurityProperties securityProperties,
                          ApplicationContext applicationContext, ResourceLoader resourceLoader) {
        this.databaseProperties = databaseProperties;
        this.securityProperties = securityProperties;
        this.applicationContext = applicationContext;
        this.resourceLoader = resourceLoader;

        configurations = new ConcurrentHashMap<>();
        dataSource = new RoutingDataSource();
    }

    @Bean
    public DataSource dataSource() throws IOException {
        dataSource.setTargetDataSources(createTargetDataSource());
        dataSource.setDefaultTargetDataSource(configurations.get(DBContextHolder.DEFAULT_DATASOURCE));

        return dataSource;
    }

    /**
     * Saves data sources to storage.
     *
     * @return newly filled storage, containing all available data sources.
     * @throws IOException if something goes wrong during loading of data sources.
     */
    public Map<Object, Object> createTargetDataSource() throws IOException {
        loadDataSources().forEach(this::addDataSource);
        return configurations;
    }

    /**
     * Loads all available data sources.
     *
     * @return list of data source names. If none are found then default data source is returned.
     * @throws IOException if something goes wrong during accessing folder with data sources.
     */
    private List<String> loadDataSources() throws IOException {
        List<String> dataSources = Arrays.stream(applicationContext.getResources("file:database/*.mv.db"))
                .map(file -> Objects.requireNonNull(file.getFilename()).split("\\.")[0])
                .collect(Collectors.toList());

        return dataSources.isEmpty() ? Collections.singletonList(DBContextHolder.DEFAULT_DATASOURCE) : dataSources;
    }

    /**
     * Saves newly created data source and initializes it.
     *
     * @param username data source identifier.
     */
    private void addDataSource(String username) {
        DataSource dataSource = databaseProperties.dataSource(username, DBContextHolder.DEFAULT_DATASOURCE);

        configurations.put(username, dataSource);
        initDataSource(dataSource);
    }

    /**
     * Initializes data source by executing sql script to create necessary tables and insert sample data.
     *
     * @param dataSource datasource without tables.
     */
    private void initDataSource(DataSource dataSource) {
        try {
            new ResourceDatabasePopulator(resourceLoader.getResource("classpath:create.sql")).execute(dataSource);
        } catch (ScriptException ignored) {}

        try {
            new ResourceDatabasePopulator(resourceLoader.getResource("classpath:data.sql")).execute(dataSource);
        } catch (ScriptException ignored) {}
    }

    /**
     * RoutingDataSource responsible for the lookup of database.
     */
    public class RoutingDataSource extends AbstractRoutingDataSource {
        @Override
        protected Object determineCurrentLookupKey() {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes == null) return DBContextHolder.DEFAULT_DATASOURCE;

            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(attributes)).getRequest();

            String datasourceHeader = request.getHeader("X-Datasource");
            if (datasourceHeader != null && datasourceHeader.equals("default")) return DBContextHolder.DEFAULT_DATASOURCE;

            String token = request.getHeader(securityProperties.getAuthHeader());
            if (token == null || token.equals(securityProperties.getAuthTokenPrefix() + "null")) {
                return DBContextHolder.DEFAULT_DATASOURCE;
            }

            String username = Jwts.parserBuilder()
                    .setSigningKey(securityProperties.getJwtSecret().getBytes()).build()
                    .parseClaimsJws(token.replace(securityProperties.getAuthTokenPrefix(), ""))
                    .getBody().getSubject();
            if(username == null) return DBContextHolder.DEFAULT_DATASOURCE;

            String dataSourceName = DBContextHolder.generateDataSourceName(username);
            if (!configurations.containsKey(dataSourceName)) addDataSource(dataSourceName);

            dataSource.afterPropertiesSet();
            LOGGER.info(String.format("DataSource: %s", configurations.get(dataSourceName)));

            DBContextHolder.setContext(username);
            return DBContextHolder.getContext();
        }
    }

    /**
     * Context holder of active data source.
     */
    public static class DBContextHolder {
        private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();
        private static final String DEFAULT_DATASOURCE = "db";

        public static String getContext() {
            return DBContextHolder.CONTEXT.get();
        }

        public static void setContext(String context) {
            CONTEXT.set(generateDataSourceName(context));
        }

        /**
         * Generates data source identifier.
         *
         * @param username identifier of user.
         * @return a string representing hashed username.
         * @throws DataSourceException if something goes wrong during lookup of hashing algorithm.
         */
        private static String generateDataSourceName(String username) throws DataSourceException {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                messageDigest.update(username.getBytes());
                return DatatypeConverter.printHexBinary(messageDigest.digest());
            } catch (NoSuchAlgorithmException e) {
                throw new DataSourceException(
                        String.format("Problem occurred during hashing of username: %s", e.getMessage()));
            }
        }

        /**
         * Patches data source identifier.
         *
         * @param oldUsername non-hashed old identifier.
         * @param newUsername non-hashed new identifier.
         * @throws IOException if something goes wrong during changing file names.
         */
        public static void patchDataSourceName(String oldUsername, String newUsername) throws IOException {
            String[] extensions = new String[]{"mv", "trace"};
            for (String extension : extensions) {
                Path source = Paths.get("database/" + generateDataSourceName(oldUsername) + "." + extension + ".db");
                Files.move(source, source.resolveSibling(generateDataSourceName(newUsername) + "." + extension + ".db"));
            }
        }
    }
}
