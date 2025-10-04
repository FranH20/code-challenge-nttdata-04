package com.nttdata.fhuichic.service;

import com.nttdata.fhuichic.dto.EarthquakeIntensityDto;
import com.nttdata.fhuichic.model.Earthquake;
import io.smallrye.mutiny.Uni;

import java.time.LocalDate;

public interface EarthquakeService {

    Uni<Void> save(Earthquake earthquake);

    Uni<EarthquakeIntensityDto> findByGeolocalizationByLatitudeAndLongitudeAndRadiusAndDate(Double lat, Double lon, Double radius, LocalDate date);

}
