package com.nttdata.fhuichic.controller;

import com.nttdata.fhuichic.dto.EarthquakeDto;
import com.nttdata.fhuichic.mapper.EarthquakeMapper;
import com.nttdata.fhuichic.model.Earthquake;
import com.nttdata.fhuichic.service.EarthquakeService;
import com.nttdata.fhuichic.sse.SseBroadcaster;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;

import java.time.LocalDate;

@ApplicationScoped
@Path("/earthquakes")
public class EarthquakeRestController {

    @Inject
    EarthquakeService earthquakeService;
    @Inject
    EarthquakeMapper earthquakeMapper;
    @Inject
    SseBroadcaster broadcaster;

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> saveEarthquake(@Valid EarthquakeDto request) {
        Earthquake earthquake = earthquakeMapper.toDto(request);
        return earthquakeService.save(earthquake)
                .onItem().transform(savedEarthquake -> Response.status(Response.Status.NO_CONTENT).build()
                ).onFailure().recoverWithItem(throwable ->
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity(throwable.getMessage())
                                .build());
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getEarthquakeByDate(
            @QueryParam("latitude") @NotNull Double latitude,
            @QueryParam("longitude") @NotNull Double longitude,
            @QueryParam("radius") @NotNull @Positive Double radius,
            @QueryParam("date") @NotNull LocalDate date) {
        return earthquakeService.findByGeolocalizationByLatitudeAndLongitudeAndRadiusAndDate(latitude, longitude, radius, date)
                .onItem().transform(earthquakes -> Response.ok(earthquakes).build()
                ).onFailure().recoverWithItem(throwable ->
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity(throwable.getMessage())
                                .build());

    }

    @GET
    @Path("/realtime")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<OutboundSseEvent> getEarthquakeRealTime() {
        return broadcaster.broadcast()
                .onFailure().retry().atMost(3)
                .onCancellation().invoke(() -> Log.info("Client disconnected"));
    }

}