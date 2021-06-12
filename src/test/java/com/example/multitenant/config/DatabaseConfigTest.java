package com.example.multitenant.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class DatabaseConfigTest {

    @Autowired
    private DatabaseConfig databaseConfig;

    @Test
    @DisplayName("Creating sql file should save file to filesystem.")
    public void creatingSqlFile_shouldSaveFileToFilesystem() throws IOException {
        Path path = Path.of("src/main/resources/create.sql");

        assertTrue(Files.exists(path));

        Files.delete(path);
        assertFalse(Files.exists(path));

        databaseConfig.createDataSourceSchema(databaseConfig.generateMetadata());
        assertTrue(Files.exists(path));
    }

    @Test
    @DisplayName("Creating datasource should save database to filesystem.")
    public void creatingDataSource_shouldSaveDatabaseToFilesystem() throws IOException {
        String username = "test";
        String dataSourceName = DatabaseConfig.DBContextHolder.generateDataSourceName(username);
        Path path = Path.of("database-test/" + dataSourceName + ".mv.db");

        assertFalse(Files.exists(path));
        databaseConfig.setActiveDatasource(username);

        Map<Object, Object> configurations = DatabaseConfig.getConfigurations();
        configurations.remove(dataSourceName);

        assertTrue(Files.exists(path));
        databaseConfig.setActiveDatasource(username);

        Files.delete(path);
    }
}
