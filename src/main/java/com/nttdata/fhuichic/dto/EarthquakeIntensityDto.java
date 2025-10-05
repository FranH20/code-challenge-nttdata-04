package com.nttdata.fhuichic.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public record EarthquakeIntensityDto(
        Double maxIntensity,
        Double minIntensity,
        List<EarthquakeDto> events
) {
}
