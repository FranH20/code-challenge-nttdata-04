package com.nttdata.fhuichic.kafka;

import com.nttdata.fhuichic.sse.SseBroadcaster;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EarthquakeKafkaTest {

    @InjectMocks
    EarthquakeKafka earthquakeKafka;

    @Mock
    SseBroadcaster broadcaster;

    @Test
    void send() {
    }

    @Test
    @DisplayName("Dado un evento Kafka, cuando se recibe, debe ejecutarse correctamente")
    void earthquakeIn() {
        String earthquake = """
                {
                  "intensity": 5.8,
                  "deepness": 10.5,
                  "geo": {
                    "latitude": -33.4489,
                    "longitude": -70.6693
                  }
                }
                """;
        Mockito.when(broadcaster.sendEvent(Mockito.anyString()))
                .thenReturn(Uni.createFrom().voidItem());

        earthquakeKafka.earthquakeIn(earthquake).await().indefinitely();

        Mockito.verify(broadcaster, Mockito.times(1)).sendEvent(Mockito.anyString());
    }
}