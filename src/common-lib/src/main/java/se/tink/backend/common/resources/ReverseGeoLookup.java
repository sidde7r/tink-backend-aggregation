package se.tink.backend.common.resources;

import java.util.Optional;
import java.util.Locale;
import se.tink.backend.common.merchants.GooglePlacesSearcher;
import se.tink.backend.core.Coordinate;

public class ReverseGeoLookup {
    private static final Locale ADDRESS_LOCALE = new Locale("sv", "SE");
    private final GooglePlacesSearcher googlePlacesSearcher;

    public ReverseGeoLookup(GooglePlacesSearcher googlePlacesSearcher) {
        this.googlePlacesSearcher = googlePlacesSearcher;
    }

    public Optional<Coordinate> getCoordinate(String address) throws Exception {
        return googlePlacesSearcher.getCoordinate(address, ADDRESS_LOCALE.toString());
    }
}
