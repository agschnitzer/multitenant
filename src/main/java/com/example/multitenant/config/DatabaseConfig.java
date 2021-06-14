package com.example.multitenant.config;

import com.example.multitenant.config.properties.DatabaseProperties;
import com.example.multitenant.entity.User;
import com.github.fluent.hibernate.cfg.scanner.EntityScanner;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Map<Object, Object> configurations = new HashMap<>();
    private static final String ENTITY_PACKAGE = "com.example.multitenant.entity";
    private static final String CREATE_TENANT_URI = "src/main/resources/create.sql";
    private static final String CREATE_DEFAULT_URI = "src/main/resources/create_default.sql";

    private final DatabaseProperties databaseProperties;
    private final ResourceLoader resourceLoader;
    private final RoutingDataSource dataSource;

    @Autowired
    public DatabaseConfig(DatabaseProperties databaseProperties, ResourceLoader resourceLoader) {
        this.databaseProperties = databaseProperties;
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

        em.setDataSource(dataSource);
        em.setPackagesToScan(ENTITY_PACKAGE);
        em.setJpaVendorAdapter(vendorAdapter);

        return em;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        MetadataSources metadataSources = generateMetadata(true);
        if (Files.notExists(Path.of(CREATE_DEFAULT_URI))) createDataSourceSchema(metadataSources);
        if (Files.notExists(Path.of(CREATE_TENANT_URI))) createDataSourceSchema(generateMetadata(false));

        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setMetadataSources(metadataSources);
        sessionFactory.setDataSource((DataSource) configurations.get(DBContextHolder.DEFAULT_DATASOURCE));
        sessionFactory.setPackagesToScan(ENTITY_PACKAGE);

        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", databaseProperties.getDdl());
        hibernateProperties.setProperty("hibernate.dialect", databaseProperties.getDialect());
        sessionFactory.setHibernateProperties(hibernateProperties);

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
    public void renameDatasource(String oldUsername, String newUsername) throws IOException {
        String oldIdentifier = DBContextHolder.generateDataSourceName(oldUsername);
        String newIdentifier = DBContextHolder.generateDataSourceName(newUsername);

        DataSource dataSource = (DataSource) configurations.remove(oldIdentifier);
        if (dataSource == null) return;

        configurations.put(newIdentifier, dataSource);
        String[] extensions = new String[]{"mv", "trace"};
        for (String extension : extensions) {
            Path source = Paths.get(databaseProperties.getDirectory() + "/" + oldIdentifier + "." + extension + ".db");
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
            if (Files.exists(Path.of(databaseProperties.getDirectory() + "/" + dataSourceName + ".mv.db"))) {
                configurations.put(dataSourceName, databaseProperties.dataSource(dataSourceName, DBContextHolder.DEFAULT_DATASOURCE));
            } else {
                addDataSource(dataSourceName);
            }
        }

        dataSource.afterPropertiesSet();
        DBContextHolder.setContext(username);
    }

    /**
     * Gets configuration. Only used for testing purposes.
     *
     * @return a map containing all data sources.
     */
    protected Map<Object, Object> getConfigurations() {
        return configurations;
    }

    /**
     * Creates sql file from schema.
     *
     * @param metadataSources containing annotated classes.
     */
    protected void createDataSourceSchema(MetadataSources metadataSources) {
        SchemaExport schemaExport = new SchemaExport();
        List<Class<?>> classes = new ArrayList<>(metadataSources.getAnnotatedClasses());

        schemaExport.setFormat(true);
        schemaExport.setDelimiter(";");
        schemaExport.setOutputFile(classes.size() == 1 && classes.contains(User.class) ? CREATE_DEFAULT_URI : CREATE_TENANT_URI);
        schemaExport.createOnly(EnumSet.of(TargetType.SCRIPT), metadataSources.buildMetadata());
    }

    /**
     * Generates metadata needed for creation of schema.
     *
     * @param isDefault true if schema should only contain user table.
     * @return metadata containing settings and annotated classes.
     */
    protected MetadataSources generateMetadata(Boolean isDefault) {
        Map<String, String> settings = new HashMap<>();
        settings.put("connection.driver_class", databaseProperties.getDriverClassName());
        settings.put("dialect", databaseProperties.getDialect());
        settings.put("hibernate.connection.url", databaseProperties.getUrl());
        settings.put("hibernate.connection.username", databaseProperties.getUsername());
        settings.put("hibernate.connection.password", databaseProperties.getPassword());

        MetadataSources metadataSources = new MetadataSources(
                new StandardServiceRegistryBuilder().applySettings(settings).build()
        );

        if (isDefault) {
            metadataSources.addAnnotatedClass(User.class);
            return metadataSources;
        }

        for (Class<?> annotatedClass : EntityScanner.scanPackages(ENTITY_PACKAGE).result()) {
            if (annotatedClass != User.class) metadataSources.addAnnotatedClass(annotatedClass);
        }

        return metadataSources;
    }

    /**
     * Loads list containing each datasource.
     *
     * @return list of datasource names. If none are found, the default datasource is returned.
     * @throws IOException if something goes wrong during accessing the specified path.
     */
    private List<String> loadDataSources() throws IOException {
        List<String> dataSources = Files.list(Path.of(databaseProperties.getDirectory() + "/"))
                .filter(file -> file.getFileName().toString().endsWith("mv.db"))
                .map(file -> file.getFileName().toString().split("\\.")[0])
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

        try {
            String filename = hashedUsername.equals(DBContextHolder.DEFAULT_DATASOURCE) ?
                    "classpath:create_default.sql" : "classpath:create.sql";
            new ResourceDatabasePopulator(resourceLoader.getResource(filename)).execute(dataSource);
        } catch (ScriptException ignored) {}
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
        public static void setContext(String context) {
            String identifier = generateDataSourceName(context);

            CONTEXT.set(identifier);
            LOGGER.debug("Datasource Identifier: {}", identifier);
        }

        /**
         * Generates datasource identifier.
         *
         * @param username identifier of user.
         * @return a string representing the hashed username.
         */
        protected static String generateDataSourceName(String username) {
            MessageDigest messageDigest = null;

            try {
                messageDigest = MessageDigest.getInstance("SHA-256");
                messageDigest.update(username.getBytes());
            } catch (NoSuchAlgorithmException ignored) {}

            assert messageDigest != null;
            return DatatypeConverter.printHexBinary(messageDigest.digest());
        }
    }

    /**
     * RoutingDataSource: responsible for the lookup of database.
     */
    private static class RoutingDataSource extends AbstractRoutingDataSource {
        @Override
        protected Object determineCurrentLookupKey() {
            String context = DBContextHolder.CONTEXT.get();
            LOGGER.debug("Current datasource: " + context);
            return context;
        }
    }
}
