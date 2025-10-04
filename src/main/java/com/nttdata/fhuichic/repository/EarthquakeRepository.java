package com.nttdata.fhuichic.repository;

import com.nttdata.fhuichic.model.Earthquake;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class EarthquakeRepository implements PanacheRepository<Earthquake> {

    public Uni<Earthquake> save(Earthquake earthquake) {
        return persist(earthquake);
    }

    public Uni<List<Earthquake>> getByDate(LocalDate date) {
        return find("date = ?1",  date).list();
    }

}
