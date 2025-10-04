package com.nttdata.fhuichic;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String POSTGRES_IMAGE = "postgres:15-alpine";
    private PostgreSQLContainer<?> postgres;

    @Override
    public Map<String, String> start() {
        System.out.println("🚀 INICIANDO POSTGRESQL TESTCONTAINER...");

        postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")
                .withReuse(true);

        postgres.start();

        String jdbcUrl = postgres.getJdbcUrl();
        String reactiveUrl = jdbcUrl.replace("jdbc:", "vertx-reactive:");

        System.out.println("✅ POSTGRESQL INICIADO EN: " + reactiveUrl);

        Map<String, String> config = Map.of(
                "quarkus.datasource.reactive.url", reactiveUrl,
                "quarkus.datasource.username", postgres.getUsername(),
                "quarkus.datasource.password", postgres.getPassword()
        );

        System.out.println("🔧 CONFIGURACIÓN:");
        config.forEach((k, v) -> System.out.println("  " + k + " = " + v));

        return config;
    }

    @Override
    public void stop() {
        if (postgres != null) {
            System.out.println("🛑 DETENIENDO POSTGRESQL TESTCONTAINER...");
            postgres.stop();
        }
    }
}
