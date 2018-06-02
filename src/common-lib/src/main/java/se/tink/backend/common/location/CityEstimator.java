package se.tink.backend.common.location;

import se.tink.backend.core.User;

import java.util.Date;
import java.util.List;

public interface CityEstimator {

    public LocationGuessType getType();

    public List<CityLocationGuess> estimate(User user, Date date);

    public List<CityLocationGuess> estimate(User user, Date date, int daysRadius);

}
