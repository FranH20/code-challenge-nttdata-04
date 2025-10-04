package com.nttdata.fhuichic.service;

import com.nttdata.fhuichic.dto.EarthquakeDto;
import com.nttdata.fhuichic.dto.EarthquakeIntensityDto;
import com.nttdata.fhuichic.kafka.EarthquakeKafka;
import com.nttdata.fhuichic.mapper.EarthquakeMapper;
import com.nttdata.fhuichic.model.Earthquake;
import com.nttdata.fhuichic.model.Geography;
import com.nttdata.fhuichic.repository.EarthquakeRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static com.nttdata.fhuichic.util.Utils.haversine;

@ApplicationScoped
public class EarthquakeServiceHandler implements EarthquakeService {

    @Inject
    EarthquakeRepository earthquakeRepository;
    @Inject
    EarthquakeKafka earthquakeKafka;
    @Inject
    EarthquakeMapper earthquakeMapper;

    @Override
    @WithTransaction
    public Uni<Void> save(Earthquake earthquake) {
        return earthquakeRepository.save(earthquake)
                .onItem().call(this::sendToKafka)
                .onItem().invoke(savedEarthquake -> Log.infof("Earthquake saved with ID: %s", savedEarthquake.getId()))
                .onFailure().invoke(throwable -> Log.errorf("Error saving Earthquake: %s", throwable.getMessage()))
                .replaceWithVoid();
    }

    @Override
    @WithSession
    public Uni<EarthquakeIntensityDto> findByGeolocalizationByLatitudeAndLongitudeAndRadiusAndDate(Double lat, Double lon, Double radius, LocalDate date) {
        return earthquakeRepository.getByDate(date)
                .map(list -> filterByGeolocalizationAndRadius(list, lat, lon, radius))
                .map(this::calculateIntensity)
                .onItem().invoke(filteredStream -> Log.infof("Filtered earthquakes count: %d", filteredStream.events().size()))
        .onFailure().invoke(throwable -> Log.errorf("Error filtering earthquakes: %;s", throwable.getMessage()));
    }

    private List<EarthquakeDto> filterByGeolocalizationAndRadius(List<Earthquake> earthquakes, Double lat, Double lon, Double radius) {
        return earthquakes.stream()
                .filter(earthquake -> Objects.nonNull(earthquake.getGeography()))
                .filter(earthquake -> {
                    Geography geography = earthquake.getGeography();
                    if (geography.getLatitude() == null || geography.getLongitude() == null) {
                        return false;
                    }
                    double distance = haversine(lat, lon, geography.getLatitude(), geography.getLongitude());
                    return distance <= radius;
                })
                .map(earthquakeMapper::earthquakeToDto)
                .toList();
    }

    private EarthquakeIntensityDto calculateIntensity(List<EarthquakeDto> filteredList) {
        double maxIntensity = filteredList.stream()
                .mapToDouble(EarthquakeDto::intensity)
                .max()
                .orElse(0.0);
        double minIntensity = filteredList.stream()
                .mapToDouble(EarthquakeDto::intensity)
                .min()
                .orElse(0.0);
        return new EarthquakeIntensityDto(maxIntensity, minIntensity, filteredList);
    }

    private Uni<Void> sendToKafka(Earthquake savedEarthquake) {
        return earthquakeKafka.send(savedEarthquake)
                .onItem().invoke(success -> Log.infof("Successfully sent to Kafka: %s", savedEarthquake.getId()))
                .onFailure().invoke(throwable ->
                        Log.errorf("Failed to send to Kafka: %s. Error: %s", savedEarthquake.getId(), throwable.getMessage()))
                .onFailure().recoverWithUni(throwable -> Uni.createFrom().emitter(emitter -> {
                    Log.warnf("Kafka send failed for Earthquake %s, recovering gracefully.", savedEarthquake.getId());
                    emitter.complete(null);
                }));
    }
}
