package se.tink.backend.common.location.facebook;


import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.location.CityLocationGuess;
import se.tink.backend.common.location.LocationGuessType;
import se.tink.backend.common.location.LocationTestUtils;
import se.tink.backend.common.repository.mysql.main.UserFacebookProfileRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.UserFacebookProfile;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FacebookBasedCityEstimatorTest {

    FacebookBasedCityEstimator estimator;
    UserFacebookProfileRepository profileRepository;
    ServiceContext serviceContext;
    User user;

    @Before
    public void setUp() {

        profileRepository = mock(UserFacebookProfileRepository.class);
        user = mock(User.class);
        serviceContext = mock(ServiceContext.class);

        when(user.getId()).thenReturn("userId");
        when(serviceContext.getRepository(UserFacebookProfileRepository.class)).thenReturn(profileRepository);

        estimator = new FacebookBasedCityEstimator(serviceContext);
    }


    @Test
    public void testReturnNoFacebookUser() {

        when(profileRepository.findByUserId("userId")).thenReturn(null);

        List<CityLocationGuess> guesses = estimator.estimate(user, new Date());

        assertEquals(0, guesses.size());
    }

    @Test
    public void testReturnFacebookUserWithoutLocation() {

        UserFacebookProfile profile = mock(UserFacebookProfile.class);
        when(profile.getLocationName()).thenReturn(null);
        when(profileRepository.findByUserId("userId")).thenReturn(profile);

        List<CityLocationGuess> guesses = estimator.estimate(user, new Date());

        assertEquals(0, guesses.size());
    }


    @Test
    public void testStockholmSweden() {

        UserFacebookProfile profile = mock(UserFacebookProfile.class);
        when(profile.getLocationName()).thenReturn("Stockholm, Sweden");
        when(profileRepository.findByUserId("userId")).thenReturn(profile);

        List<CityLocationGuess> guesses = estimator.estimate(user, new Date());

        assertEquals(1, guesses.size());
        LocationTestUtils.verifyCityLocationGuess(guesses.get(0), "Stockholm", 1f, LocationGuessType.FACEBOOK_HOME);
    }

    @Test
    public void testSkovde() {

        UserFacebookProfile profile = mock(UserFacebookProfile.class);
        when(profile.getLocationName()).thenReturn("Skövde");
        when(profileRepository.findByUserId("userId")).thenReturn(profile);

        List<CityLocationGuess> guesses = estimator.estimate(user, new Date());

        assertEquals(1, guesses.size());
        LocationTestUtils.verifyCityLocationGuess(guesses.get(0), "Skövde", 1f, LocationGuessType.FACEBOOK_HOME);
    }

    @Test
    public void testSodermalmStockholmsLanSweden() {

        UserFacebookProfile profile = mock(UserFacebookProfile.class);
        when(profile.getLocationName()).thenReturn("Södermalm, Stockholms län, Sweden");
        when(profileRepository.findByUserId("userId")).thenReturn(profile);

        List<CityLocationGuess> guesses = estimator.estimate(user, new Date());

        assertEquals(1, guesses.size());
        LocationTestUtils.verifyCityLocationGuess(guesses.get(0), "Södermalm", 1f, LocationGuessType.FACEBOOK_HOME);
    }

}
