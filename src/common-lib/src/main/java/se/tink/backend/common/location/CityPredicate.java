package se.tink.backend.common.location;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;

public class CityPredicate implements Predicate<CityLocationGuess> {

    private String city;
    public CityPredicate(String city) {
        this.city = city;
    }

    @Override
    public boolean apply(@Nullable CityLocationGuess s) {
        return city.equals(s.getCity());
    }
}

