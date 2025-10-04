package com.nttdata.fhuichic.sse;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SseBroadcaster {

    private final Map<String, BroadcastProcessor<OutboundSseEvent>> processors = new ConcurrentHashMap<>();
    private final static String PROCESS_ID = "all";

    @Inject
    Sse sse;

    public Multi<OutboundSseEvent> broadcast() {
        Log.debugf("Broadcasting process %s", PROCESS_ID);
        BroadcastProcessor<OutboundSseEvent> processor = processors.computeIfAbsent(PROCESS_ID, id -> BroadcastProcessor.create());
        return Multi.createBy().concatenating().streams(
                    Multi.createFrom().item(welcomeEvent()),
                    processor
                )
                .onCancellation().invoke(() -> {
                    Log.debugf("Cancelling process %s", PROCESS_ID);
                }).onFailure().invoke(throwable -> {
                    Log.errorf("Error in SSE stream: %s", throwable.getMessage());
                });
    }

    public Uni<Void> sendEvent(String data) {
        Log.info("Intentando enviar un evento por SSE...");
        BroadcastProcessor<OutboundSseEvent> processor = processors.get(PROCESS_ID);
        if (processor != null) {
            Log.info("Proceso encontrado, enviando evento");
            OutboundSseEvent event = sse.newEventBuilder()
                    .name("tremor-events")
                    .data(data)
                    .id(UUID.randomUUID().toString())
                    .mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .build();
            processor.onNext(event);
            Log.info("Evento enviado por SSE");
        } else {
            Log.error("No se encontró un proceso activo para enviar el evento");
        }
        return Uni.createFrom().nullItem();
    }

    private OutboundSseEvent welcomeEvent() {
        return sse.newEventBuilder()
                .name("connected")
                .data("{\"status\":\"connected\",\"message\":\"SSE connection established\"}")
                .id(UUID.randomUUID().toString())
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .build();
    }
}
