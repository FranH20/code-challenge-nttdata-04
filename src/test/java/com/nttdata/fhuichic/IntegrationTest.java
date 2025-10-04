package com.nttdata.fhuichic;

import com.nttdata.fhuichic.dto.EarthquakeDto;
import com.nttdata.fhuichic.sse.SseBroadcaster;
import com.nttdata.fhuichic.utils.TestUtils;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(KafkaTestResource.class)
@Testcontainers
public class IntegrationTest {

    @Inject
    SseBroadcaster broadcaster;

    @Test
    @DisplayName("Dado un EarthquakeDto válido, cuando se llama al endpoint /earthquake/, entonces se guarda correctamente y se recibe un 204")
    void testSaveEarthquake_Success() {
        EarthquakeDto requestBody = TestUtils.generateObject("data/integration/earthquake-01.json", EarthquakeDto.class);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/earthquakes/")
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("Dado un EarthquakeDto inválido, cuando se llama al endpoint /earthquakes/, entonces no se guarda y se recibe un 400")
    void testSaveEarthquake_NullData() {
        EarthquakeDto requestBody = TestUtils.generateObject("data/integration/earthquake-error.json", EarthquakeDto.class);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/earthquakes/")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Dado que existen terremotos en la base de datos, cuando se llama al endpoint /earthquakes/ con parámetros de búsqueda, entonces se reciben los terremotos que coinciden y un 200")
    void testGetEarthquakeByDate_Success() {
        String[] files = {
                "data/integration/earthquake-01.json",
                "data/integration/earthquake-02.json",
                "data/integration/earthquake-03.json"
        };
        for (String file : files) {
            EarthquakeDto requestBody = TestUtils.generateObject(file, EarthquakeDto.class);

            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/earthquakes/")
                    .then()
                    .statusCode(204);
        }

        given()
                .contentType(ContentType.JSON)
                .queryParam("latitude", -33.4489)
                .queryParam("longitude", -70.6693)
                .queryParam("radius", 10)
                .queryParam("date", LocalDate.now().toString())
                .when()
                .get("/earthquakes/")
                .then()
                .statusCode(200)
                .body("maxIntensity", Matchers.equalTo(8.2F))
                .body("minIntensity", Matchers.equalTo(3.1F))
                .body("events.size()", Matchers.equalTo(3));
    }

    @Test
    @DisplayName("Dado que no existen terremotos en la base de datos, cuando se llama al endpoint /earthquakes/ con parámetros de búsqueda, entonces se recibe una lista vacía y un 200")
    void testGetEarthquakeByDate_EmptyResult() {
        String[] files = {
                "data/integration/earthquake-01.json",
                "data/integration/earthquake-02.json",
                "data/integration/earthquake-03.json"
        };
        for (String file : files) {
            EarthquakeDto requestBody = TestUtils.generateObject(file, EarthquakeDto.class);

            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/earthquakes/")
                    .then()
                    .statusCode(204);
        }

        given()
                .contentType(ContentType.JSON)
                .queryParam("latitude", 40.7128)
                .queryParam("longitude", -74.0060)
                .queryParam("radius", 5)
                .queryParam("date", LocalDate.now().toString())
                .when()
                .get("/earthquakes/")
                .then()
                .statusCode(200)
                .body("maxIntensity", Matchers.equalTo(0F))
                .body("minIntensity", Matchers.equalTo(0F))
                .body("events.size()", Matchers.equalTo(0));
    }

    @Test
    @DisplayName("Cuando se llama al endpoint /earthquakes/realtime, entonces se establece una conexión SSE y se recibe un 200")
    void testGetEarthquakeRealTime_SseEndpoint() throws Exception {
        System.out.println("🧪 TEST SSE - FLUJO CORRECTO");

        EarthquakeDto requestBody = TestUtils.generateObject("data/integration/earthquake-01.json", EarthquakeDto.class);

        CompletableFuture.supplyAsync(() -> {
            try {
                return given()
                        .when()
                        .get("/earthquakes/realtime")
                        .then()
                        .statusCode(200)
                        .contentType("text/event-stream")
                        .extract()
                        .asString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Thread.sleep(1000);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/earthquakes/")
                .then()
                .statusCode(204);

    }
}