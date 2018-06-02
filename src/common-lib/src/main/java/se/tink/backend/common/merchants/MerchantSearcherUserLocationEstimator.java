package se.tink.backend.common.merchants;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.location.AggregatedLocationEstimator;
import se.tink.backend.common.location.CityLocationGuess;
import se.tink.backend.common.location.UserLocationEstimator;
import se.tink.backend.common.location.facebook.FacebookBasedCityEstimator;
import se.tink.backend.common.location.transaction.TransactionBasedCityEstimator;
import se.tink.backend.common.repository.cassandra.UserLocationRepository;
import se.tink.backend.common.repository.mysql.main.CityCoordinateRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.CityCoordinate;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserLocation;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.guavaimpl.Predicates;

public class MerchantSearcherUserLocationEstimator {

    private static final float FACEBOOK_ESTIMATOR_WEIGHT = 0.4f;
    private static final float TRANSACTIONAL_ESTIMATOR_WEIGHT = 0.6f;
    // private static final float CIVIL_REGISTRY_ESTIMATOR_WEIGHT = 0.3f;

    private UserLocationEstimator userLocationEstimator;
    private UserLocationRepository userLocationRepository;
    private CityCoordinateRepository cityCoordinateRepository;
    private Supplier<Map<String, String>> merchantsCityByIdSupplier;
    private FacebookBasedCityEstimator facebookLocationEstimator;

    public MerchantSearcherUserLocationEstimator(final ServiceContext context) {
        this.userLocationRepository = context.getRepository(UserLocationRepository.class);
        this.cityCoordinateRepository = context.getRepository(CityCoordinateRepository.class);

        this.userLocationEstimator = new UserLocationEstimator();
        facebookLocationEstimator = new FacebookBasedCityEstimator(context);

        merchantsCityByIdSupplier = Suppliers.memoizeWithExpiration(() -> {
            Iterable<Merchant> merchantsWithCity = Iterables.filter(context.getRepository(MerchantRepository.class)
                    .findAll(), Predicates.MERCHANT_HAS_CITY);
            Map<String, Merchant> merchantsById = Maps.uniqueIndex(merchantsWithCity, Merchant::getId);
            return Maps.transformValues(merchantsById, Merchant::getCity);
        }, 30, TimeUnit.MINUTES);

    }

    public UserLocation getUserLocation(User user, Date date, List<Transaction> transactions) {

        UserLocation location = getEstimatedUserDeviceLocation(user, date);

        if (location == null) {
            // If we didn't find User device Location, estimate location based on other sources
            location = getEstimatedCityUserLocation(user, date, transactions);
        }

        return location;
    }

    private UserLocation getEstimatedCityUserLocation(User user, Date date, List<Transaction> transactions) {

        AggregatedLocationEstimator aggregatedCityEstimator = new AggregatedLocationEstimator();
        TransactionBasedCityEstimator transactional = new TransactionBasedCityEstimator(
                merchantsCityByIdSupplier.get(), transactions);
        
        aggregatedCityEstimator.addLocationEstimator(transactional, TRANSACTIONAL_ESTIMATOR_WEIGHT);
        aggregatedCityEstimator.addLocationEstimator(facebookLocationEstimator, FACEBOOK_ESTIMATOR_WEIGHT);
        CityLocationGuess mostProbableLocation = aggregatedCityEstimator.getMostProbableLocation(user, date);

        UserLocation location = null;
        
        if (mostProbableLocation != null && !Strings.isNullOrEmpty(mostProbableLocation.getCity())) {
            String city = mostProbableLocation.getCity();

            CityCoordinate cc = cityCoordinateRepository.findOneByCityAndCountry(city, "Sverige");

            if (cc != null) {
                location = new UserLocation();
                location.setDate(date);
                location.setUserId(UUIDUtils.fromTinkUUID(user.getId()));

                location.setLatitude(cc.getCoordinate().getLatitude());
                location.setLongitude(cc.getCoordinate().getLongitude());
            }
        }

        return location;
    }

    /**
     * Gets the estimated user location for a specific transaction
     * 
     * @param user
     *            User to find locations for
     * @param date
     * @return Estimated location if it could be found, else null.
     */
    private UserLocation getEstimatedUserDeviceLocation(User user, Date date) {
        Date start = DateUtils.setInclusiveStartTime(date);
        Date end = DateUtils.inclusiveEndTime(date);

        // Get locations within range
        List<UserLocation> locations = userLocationRepository.findAllByUserIdAndDateBetween(user.getId(), start, end);

        // Get estimated location for the transaction day
        return userLocationEstimator.getEstimatedLocationForDate(user, locations, date);
    }
}
