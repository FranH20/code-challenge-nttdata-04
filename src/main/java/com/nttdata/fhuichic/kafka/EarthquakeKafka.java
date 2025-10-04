package com.nttdata.fhuichic.kafka;

import com.nttdata.fhuichic.model.Earthquake;
import com.nttdata.fhuichic.sse.SseBroadcaster;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@Slf4j
@ApplicationScoped
public class EarthquakeKafka {

    @Channel("tremor-out")
    MutinyEmitter<Earthquake> emitter;

    @Inject
    SseBroadcaster broadcaster;

    public Uni<Void> send(Earthquake earthquake) {
        log.info("Sending tremor to Kafka tremor topic: {}", earthquake.getId());
        return emitter.send(earthquake);
    }

    @Incoming("tremor-in")
    public Uni<Void> earthquakeIn(String jsonEarthquake) {
        log.info("Recibe Kafka event: {}", jsonEarthquake);
        return Uni.createFrom().item(jsonEarthquake)
                .onItem().call(receivedEarthquake -> broadcaster.sendEvent(jsonEarthquake))
                .onItem().ignore().andContinueWithNull()
                .onFailure().invoke(throwable -> log.error("Kafka error processing: {}", throwable.getMessage()))
                .onFailure().recoverWithNull();
    }

}
