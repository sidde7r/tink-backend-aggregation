package se.tink.backend.common.location.facebook;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.location.CityLocationGuess;
import se.tink.backend.common.location.CityEstimator;
import se.tink.backend.common.location.LocationGuessType;
import se.tink.backend.common.repository.mysql.main.UserFacebookProfileRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.UserFacebookProfile;

import java.util.Date;
import java.util.List;

public class FacebookBasedCityEstimator implements CityEstimator {
    private UserFacebookProfileRepository profileRepository;

    public FacebookBasedCityEstimator(ServiceContext serviceContext) {
        profileRepository = serviceContext.getRepository(UserFacebookProfileRepository.class);
    }

    @Override
    public LocationGuessType getType() {
        return LocationGuessType.FACEBOOK_HOME;
    }

    @Override
    public List<CityLocationGuess> estimate(User user, Date date) {
        return estimate(user, date, 0);
    }

    @Override
    public List<CityLocationGuess> estimate(User user, Date date, int daysRadius) {
        UserFacebookProfile profile = profileRepository.findByUserId(user.getId());

        List<CityLocationGuess> guesses = Lists.newArrayList();
        if (profile == null) {
            return guesses;
        }

        String locationOriginal = profile.getLocationName();

        if (Strings.isNullOrEmpty(locationOriginal)) {
            return guesses;
        }

        CityLocationGuess guess = new CityLocationGuess(LocationGuessType.FACEBOOK_HOME);
        int index = locationOriginal.indexOf(',');
        guess.setCity(locationOriginal.substring(0, index == -1 ? locationOriginal.length() : index));

        //TODO Lower probability if date is far in past ?
        guess.setProbability(1f);
        return Lists.newArrayList(guess);
    }
}
