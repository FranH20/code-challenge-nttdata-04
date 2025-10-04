package com.nttdata.fhuichic.dto;

import io.smallrye.common.constraint.NotNull;

public record GeoDto(
        @NotNull
        Double latitude,
        @NotNull
        Double longitude
) { }
