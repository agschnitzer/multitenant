package com.example.multitenant.config;

import com.example.multitenant.config.properties.DatabaseProperties;
import com.example.multitenant.exceptionhandler.exceptions.DataSourceException;
import com.github.fluent.hibernate.cfg.scanner.EntityScanner;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
@EnableTransactionManagement
public class DatabaseConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Map<Object, Object> configurations = new ConcurrentHashMap<>();
    private static final String BASE_PACKAGE = "com.example.multitenant.entity";
    private static final String CREATE_FILE_URI = "src/main/resources/create.sql";

    private final DatabaseProperties databaseProperties;
    private final ApplicationContext applicationContext;
    private final ResourceLoader resourceLoader;
    private final RoutingDataSource dataSource;

    @Autowired
    public DatabaseConfig(DatabaseProperties databaseProperties, ApplicationContext applicationContext,
                          ResourceLoader resourceLoader) {
        this.databaseProperties = databaseProperties;
        this.applicationContext = applicationContext;
        this.resourceLoader = resourceLoader;

        dataSource = new RoutingDataSource();
    }

    @Bean
    public DataSource dataSource() throws IOException {
        loadDataSources().forEach(this::addDataSource);

        dataSource.setTargetDataSources(configurations);
        dataSource.setDefaultTargetDataSource(configurations.get(DBContextHolder.DEFAULT_DATASOURCE));

        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

        em.setDataSource((DataSource) configurations.get(DBContextHolder.DEFAULT_DATASOURCE));
        em.setPackagesToScan(BASE_PACKAGE);
        em.setJpaVendorAdapter(vendorAdapter);

        return em;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        MetadataSources metadataSources = generateMetadata();

        if (applicationContext.getResource(CREATE_FILE_URI).exists()) {
            SchemaExport schemaExport = new SchemaExport();
            schemaExport.setFormat(true);
            schemaExport.setDelimiter(";");
            schemaExport.setOutputFile(CREATE_FILE_URI);
            schemaExport.createOnly(EnumSet.of(TargetType.SCRIPT), metadataSources.buildMetadata());
        }

        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setMetadataSources(metadataSources);
        sessionFactory.setDataSource((DataSource) configurations.get(DBContextHolder.DEFAULT_DATASOURCE));
        sessionFactory.setPackagesToScan(BASE_PACKAGE);
        sessionFactory.setHibernateProperties(generateProperties());

        return sessionFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());

        return transactionManager;
    }

    /**
     * Renames datasource identifier to new username.
     *
     * @param oldUsername non-hashed old identifier.
     * @param newUsername non-hashed new identifier.
     * @throws IOException if something goes wrong during changing file names.
     */
    public static void renameDatasource(String oldUsername, String newUsername) throws IOException {
        String oldIdentifier = DBContextHolder.generateDataSourceName(oldUsername);
        String newIdentifier = DBContextHolder.generateDataSourceName(newUsername);

        Object dataSource = configurations.get(oldIdentifier);
        configurations.remove(oldIdentifier);
        HikariDataSource hikariDataSource = ((HikariDataSource) dataSource);
        if (hikariDataSource != null && hikariDataSource.isRunning()) hikariDataSource.close();

        String[] extensions = new String[]{"mv", "trace"};
        for (String extension : extensions) {
            Path source = Paths.get("database/" + oldIdentifier + "." + extension + ".db");
            if (Files.exists(source)) Files.move(source, source.resolveSibling(newIdentifier + "." + extension + ".db"));
        }
    }

    /**
     * Sets active context to datasource of current user.
     *
     * @param username non-hashed identifier of user.
     */
    public void setActiveDatasource(String username) {
        String dataSourceName = DBContextHolder.generateDataSourceName(username);
        if (!configurations.containsKey(dataSourceName)) {
            if (applicationContext.getResource("file:database/" + dataSourceName + ".mv.db").exists()) {
                configurations.put(dataSourceName, databaseProperties.dataSource(dataSourceName, DBContextHolder.DEFAULT_DATASOURCE));
            } else {
                addDataSource(dataSourceName);
            }
        }

        dataSource.afterPropertiesSet();
        DBContextHolder.setContext(username);
    }

    /**
     * Loads list containing each datasource.
     *
     * @return list of datasource names. If none are found, the default datasource is returned.
     * @throws IOException if something goes wrong during accessing the specified path.
     */
    private List<String> loadDataSources() throws IOException {
        List<String> dataSources = Arrays.stream(applicationContext.getResources("file:database/*.mv.db"))
                .map(file -> Objects.requireNonNull(file.getFilename()).split("\\.")[0])
                .collect(Collectors.toList());

        return dataSources.isEmpty() ? Collections.singletonList(DBContextHolder.DEFAULT_DATASOURCE) : dataSources;
    }

    /**
     * Saves newly created datasource and initializes it.
     *
     * @param hashedUsername datasource identifier.
     */
    private void addDataSource(String hashedUsername) {
        DataSource dataSource = databaseProperties.dataSource(hashedUsername, DBContextHolder.DEFAULT_DATASOURCE);

        configurations.put(hashedUsername, dataSource);
        initDataSource(dataSource);
    }

    /**
     * Initializes datasource by executing sql scripts to create necessary tables and insert sample data.
     *
     * @param dataSource datasource loaded from storage or created recently.
     */
    private void initDataSource(DataSource dataSource) {
        try {
            new ResourceDatabasePopulator(resourceLoader.getResource("classpath:create.sql")).execute(dataSource);
        } catch (ScriptException ignored) {}
    }

    /**
     * Generates metadata needed for creation of schema.
     *
     * @return metadata containing settings and annotated classes.
     */
    private MetadataSources generateMetadata() {
        Map<String, String> settings = new HashMap<>();
        settings.put("connection.driver_class", databaseProperties.getDriverClassName());
        settings.put("dialect", databaseProperties.getDialect());
        settings.put("hibernate.connection.url", databaseProperties.getUrl());
        settings.put("hibernate.connection.username", databaseProperties.getUsername());
        settings.put("hibernate.connection.password", databaseProperties.getPassword());

        MetadataSources metadataSources = new MetadataSources(
                new StandardServiceRegistryBuilder().applySettings(settings).build()
        );
        for (Class<?> annotatedClass : EntityScanner.scanPackages(BASE_PACKAGE).result()) {
            metadataSources.addAnnotatedClass(annotatedClass);
        }

        return metadataSources;
    }

    /**
     * Generates hibernate properties.
     *
     * @return properties containing necessary hibernate configurations.
     */
    private Properties generateProperties() {
        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", databaseProperties.getDdl());
        hibernateProperties.setProperty("hibernate.dialect", databaseProperties.getDialect());

        return hibernateProperties;
    }

    /**
     * RoutingDataSource: responsible for the lookup of database.
     */
    private static class RoutingDataSource extends AbstractRoutingDataSource {
        @Override
        protected Object determineCurrentLookupKey() {
            return DBContextHolder.CONTEXT.get();
        }
    }

    /**
     * DBContextHolder: context holder of active datasource.
     */
    public static class DBContextHolder {
        private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();
        private static final String DEFAULT_DATASOURCE = "db";

        /**
         * Sets context to default datasource.
         */
        public static void setDefault() {
            CONTEXT.set(DEFAULT_DATASOURCE);
            LOGGER.debug("Datasource Identifier: {}", DEFAULT_DATASOURCE);
        }

        /**
         * Sets context to hashed username of active user.
         *
         * @param context in form of the username.
         */
        protected static void setContext(String context) {
            String identifier = generateDataSourceName(context);

            CONTEXT.set(identifier);
            LOGGER.debug("Datasource Identifier: {}", identifier);
        }

        /**
         * Generates datasource identifier.
         *
         * @param username identifier of user.
         * @return a string representing the hashed username.
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
    }
}
