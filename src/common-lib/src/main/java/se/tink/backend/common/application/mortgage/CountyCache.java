package se.tink.backend.common.application.mortgage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.core.County;
import se.tink.backend.core.Municipality;
import se.tink.backend.utils.LogUtils;

public class CountyCache {
    private static final LogUtils log = new LogUtils(CountyCache.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final File COUNTIES_AND_MUNICIPALITIES_JSON =
            new File("data/seeding/counties-and-municipalities.json");
    private static final Supplier<List<County>> COUNTY_CACHE = Suppliers.memoizeWithExpiration(
            createCountySupplier(),
            15, TimeUnit.MINUTES);

    private static Supplier<List<County>> createCountySupplier() {
        return new Supplier<List<County>>() {
            @Override
            public List<County> get() {
                try {
                    return readCountiesFromFile();
                } catch (IOException e) {
                    log.error("Couldn't load county JSON file.", e);
                    return Collections.emptyList();
                }
            }

            private List<County> readCountiesFromFile() throws IOException {
                return OBJECT_MAPPER.readValue(COUNTIES_AND_MUNICIPALITIES_JSON, new TypeReference<List<County>>() {
                });
            }
        };
    }

    public static List<County> getCounties() {
        return CountyCache.COUNTY_CACHE.get();
    }

    public static Optional<Municipality> findMunicipality(final String municipalityCode) {
        List<County> counties = getCounties();

        for (County county : counties) {
            Optional<Municipality> municipality = county.getMunicipalities().stream().filter(municipality1 -> Objects
                    .equals(municipality1.getCode(), municipalityCode)).findFirst();

            if (municipality.isPresent()) {
                return municipality;
            }
        }

        return Optional.empty();
    }

    public static Optional<String> findMunicipalityName(String municipalityCode) {
        Optional<Municipality> municipality = findMunicipality(municipalityCode);

        if (municipality.isPresent()) {
            return Optional.of(municipality.get().getName());
        } else {
            return Optional.empty();
        }
    }
}
