package com.nttdata.fhuichic;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;

public class KafkaTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String KAFKA_IMAGE = "confluentinc/cp-kafka:7.4.0";
    private ConfluentKafkaContainer kafka;

    @Override
    public Map<String, String> start() {
        System.out.println("🚀 INICIANDO KAFKA TESTCONTAINER...");

        kafka = new ConfluentKafkaContainer(DockerImageName.parse(KAFKA_IMAGE))
                .withReuse(true)
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")  // Auto-crear tópicos
                .withEnv("KAFKA_NUM_PARTITIONS", "2")  // Particiones por defecto
                .withStartupTimeout(Duration.ofMinutes(2));
        ;

        kafka.start();

        String bootstrapServers = kafka.getBootstrapServers();
        System.out.println("✅ KAFKA INICIADO EN: " + bootstrapServers);

        try {
            kafka.execInContainer("kafka-topics", "--create",
                    "--topic", "tremor-events",
                    "--bootstrap-server", "localhost:9092",
                    "--partitions", "1",
                    "--replication-factor", "1",
                    "--if-not-exists");
            System.out.println("✅ TÓPICO 'tremor-events' CREADO");
        } catch (Exception e) {
            System.out.println("⚠️ Error creando tópico (auto-create debería manejarlo): " + e.getMessage());
        }

        Map<String, String> config = Map.of(
                "kafka.bootstrap.servers", bootstrapServers
        );

        System.out.println("🔧 SOBREESCRIBIENDO SOLO:");
        config.forEach((k, v) -> System.out.println("  " + k + " = " + v));

        return config;
    }

    @Override
    public void stop() {
        if (kafka != null) {
            System.out.println("🛑 DETENIENDO KAFKA TESTCONTAINER...");
            kafka.stop();
        }
    }
}
