package com.nttdata.fhuichic.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EarthquakeDto(
        @Positive
        @NotNull
        Double intensity,
        @Positive
        @NotNull
        Double deepness,
        @NotNull
        @Valid
        GeoDto geo
) { }
