package com.nttdata.fhuichic.mapper;

import com.nttdata.fhuichic.dto.EarthquakeDto;
import com.nttdata.fhuichic.model.Earthquake;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface EarthquakeMapper {

    @Mapping(target = "intensity", source = "intensity")
    @Mapping(target = "deepness", source = "deepness")
    @Mapping(target = "geo.latitude", source = "geography.latitude")
    @Mapping(target = "geo.longitude", source = "geography.longitude")
    EarthquakeDto earthquakeToDto(Earthquake earthquake);

    @Mapping(target = "intensity", source = "intensity")
    @Mapping(target = "deepness", source = "deepness")
    @Mapping(target = "geography.latitude", source = "geo.latitude")
    @Mapping(target = "geography.longitude", source = "geo.longitude")
    Earthquake toDto(EarthquakeDto dto);



}
