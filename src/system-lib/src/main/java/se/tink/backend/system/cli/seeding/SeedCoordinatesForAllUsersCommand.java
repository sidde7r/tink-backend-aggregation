package se.tink.backend.system.cli.seeding;

import com.google.api.client.util.Strings;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.merchants.GooglePlacesSearcher;
import se.tink.backend.common.repository.cassandra.UserCoordinatesRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.UserDemographicsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.Coordinate;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.Market;
import se.tink.backend.core.UserCoordinates;
import se.tink.backend.core.UserDemographics;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.guavaimpl.Orderings;

public class SeedCoordinatesForAllUsersCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(SeedCoordinatesForAllUsersCommand.class);

    private FraudDetailsRepository fraudDetailsRepository;
    private UserDemographicsRepository userDemographicsRepository;
    private UserRepository userRepository;
    private UserCoordinatesRepository userCoordinateRepository;

    private int maxQueries;
    private AtomicInteger numQueries;

    public SeedCoordinatesForAllUsersCommand() {
        super("seed-coords-for-users", "Seeds users_coordinates with coordinates from Google Geocode API");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        fraudDetailsRepository = serviceContext.getRepository(FraudDetailsRepository.class);
        userRepository = serviceContext.getRepository(UserRepository.class);
        userDemographicsRepository = serviceContext.getRepository(UserDemographicsRepository.class);
        userCoordinateRepository = serviceContext.getRepository(UserCoordinatesRepository.class);

        String apiKey = System.getProperty("googleApiKey");
        String maxGoogleQueries = System.getProperty("maxGoogleQueries");

        if (Strings.isNullOrEmpty(apiKey)) {
            throw new IllegalArgumentException("Need googleApiKey");
        }

        if (Strings.isNullOrEmpty(maxGoogleQueries)) {
            throw new IllegalArgumentException("Need maxGoogleQueries");
        }

        maxQueries = Integer.parseInt(maxGoogleQueries);
        numQueries = new AtomicInteger(0);

        seedDatabase(apiKey);
    }

    private void seedDatabase(final String apiKey) throws Exception {

        // Google has a limit of 50 per second but going on the safe side
        final RateLimiter rateLimiter = RateLimiter.create(40);
        final ConcurrentHashMap<String, UserCoordinates> cache = userCoordinateRepository.findAllCoordinatesByAddress();

        log.info("Starting seeding!");

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(100))
                .forEach(user -> {
                    try {
                        lookup(cache, rateLimiter, apiKey, user.getId());
                    } catch (Exception e) {
                        log.warn(user.getId(), "Caught exception while processing user.", e);
                    }
                });

        log.info("Done seeding!");
    }

    private void lookup(final ConcurrentHashMap<String, UserCoordinates> cache, final RateLimiter rateLimiter,
            final String apiKey, final String userId) {

        UserDemographics userDemographics = userDemographicsRepository.findOne(userId);

        if (userDemographics == null || !Market.Code.SE.name().equals(userDemographics.getMarket())) {
            // only run for SE users
            return;
        }

        String addressString = null;

        List<FraudDetails> addresses = fraudDetailsRepository.findAllByUserIdAndType(userId,
                FraudDetailsContentType.ADDRESS);

        if (addresses != null && addresses.size() > 0) {
            FraudDetails address = addresses.stream().max(Orderings.FRAUD_DETAILS_DATE).get();

            FraudAddressContent content = (FraudAddressContent) address.getContent();

            if (Strings.isNullOrEmpty(content.getAddress()) || Strings.isNullOrEmpty(content.getCity())) {
                log.info(userId, String.format("Address or city is null (addr: %s, city: %s).",
                        content.getAddress(), content.getCity()));
                return;
            } else if (content.getAddress().startsWith("Gatuadressakt_")) {
                log.info(userId, "Demo user, don't add");
                return;
            }

            addressString = String.format("%s, %s", cleanAddress(content.getAddress()), content.getCity());

        } else {
            //Default to lookup user's postalCode
            if (!Strings.isNullOrEmpty(userDemographics.getPostalCode()) &&
                    !Strings.isNullOrEmpty(userDemographics.getCity())) {

                addressString = String.format("%s, %s", userDemographics.getPostalCode(),
                        userDemographics.getCity());
            }
        }

        if (Strings.isNullOrEmpty(addressString)) {
            log.info(userId, "Don't have any address to lookup");
            return;
        }

        if (cache.containsKey(addressString)) {
            userCoordinateRepository.save(
                    UserCoordinates.create(userId, addressString, cache.get(addressString).getCoordinate()));
            return;
        }

        try {
            if (numQueries.incrementAndGet() > maxQueries) {
                // reached max limit of google queries
                log.debug(userId, "Reached max limit of Google queries. Skipping lookup.");
                return;
            }

            rateLimiter.acquire();

            final GooglePlacesSearcher searcher = new GooglePlacesSearcher(apiKey);
            Optional<Coordinate> coordinate = searcher.getGeocodeResultIfOne(addressString, "sv_SE");

            if (coordinate.isPresent()) {
                UserCoordinates userCoordinates = userCoordinateRepository.save(
                        UserCoordinates.create(userId, addressString, coordinate.get()));

                cache.put(userCoordinates.getAddress(), userCoordinates);
            } else {
                log.warn(userId, "Did not find or found too many results for: " + addressString);
            }
        } catch (Exception e) {
            log.warn(userId, String.format("Was not able to download coordinates for %s", addressString), e);
        }
    }

    String cleanAddress(String address) {
        int index = address.indexOf(" lgh ");
        if (index == -1) {
            return address;
        }

        return address.substring(0, index);
    }
}
