package com.nttdata.fhuichic.sse;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SseBroadcasterTest {

    @InjectMocks
    SseBroadcaster sseBroadcaster;

    @Mock
    Sse sse;

    @Mock
    OutboundSseEvent outboundSseEvent;

    @Mock
    OutboundSseEvent.Builder eventBuilder;

    @Test
    @DisplayName("Dado un Sse, cuando se llama a broadcast, debe retornar un Multi no nulo")
    void broadcast() {
        Mockito.when(sse.newEventBuilder()).thenReturn(eventBuilder);
        Mockito.when(eventBuilder.name(Mockito.anyString())).thenReturn(eventBuilder);
        Mockito.when(eventBuilder.data(Mockito.anyString())).thenReturn(eventBuilder);
        Mockito.when(eventBuilder.id(Mockito.anyString())).thenReturn(eventBuilder);
        Mockito.when(eventBuilder.mediaType(Mockito.any())).thenReturn(eventBuilder);
        Mockito.when(eventBuilder.build()).thenReturn(outboundSseEvent);

        Multi<OutboundSseEvent> result = sseBroadcaster.broadcast();
        assertNotNull(result);
    }

    @Test
    @DisplayName("Dado un Sse, cuando se llama a broadcast send event, debe retornar un Multi no nulo")
    void sendEvent() {
        Mockito.when(sse.newEventBuilder()).thenReturn(eventBuilder);
        Mockito.when(eventBuilder.name(Mockito.anyString())).thenReturn(eventBuilder);
        Mockito.when(eventBuilder.data(Mockito.anyString())).thenReturn(eventBuilder);
        Mockito.when(eventBuilder.id(Mockito.anyString())).thenReturn(eventBuilder);
        Mockito.when(eventBuilder.mediaType(Mockito.any())).thenReturn(eventBuilder);
        Mockito.when(eventBuilder.build()).thenReturn(outboundSseEvent);

        Multi<OutboundSseEvent> broadcast = sseBroadcaster.broadcast();
        AssertSubscriber<OutboundSseEvent> subscriber = AssertSubscriber.create(1);

        broadcast.subscribe().withSubscriber(subscriber);
        sseBroadcaster.sendEvent("test data").await().indefinitely();

        subscriber.assertItems(outboundSseEvent);
    }
}