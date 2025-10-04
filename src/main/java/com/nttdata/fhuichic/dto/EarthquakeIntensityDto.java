package com.nttdata.fhuichic.dto;

import java.util.List;

public record EarthquakeIntensityDto(
        Double maxIntensity,
        Double minIntensity,
        List<EarthquakeDto> events
) {
}
