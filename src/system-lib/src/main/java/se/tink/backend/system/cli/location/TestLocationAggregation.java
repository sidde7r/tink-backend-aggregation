package se.tink.backend.system.cli.location;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.location.AggregatedLocationEstimator;
import se.tink.backend.common.location.CityLocationGuess;
import se.tink.backend.common.location.LocationGuess;
import se.tink.backend.common.location.facebook.FacebookBasedCityEstimator;
import se.tink.backend.common.location.transaction.TransactionBasedCityEstimator;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class TestLocationAggregation extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(TestLocationAggregation.class);

    private Float[] weights;

    private static final int INDEX_FB = 0;
    private static final int INDEX_T = 1;

    public TestLocationAggregation() {
        super("test-location-aggregation", "Tests the user location aggregator.");

        weights = new Float[] {
                -1f, -1f, -1f
        };
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        if (!configuration.isDevelopmentMode()) {
            System.err.println("Development command, please run locally");
            return;
        }

        final String dateString = System.getProperty("date");
        final String username = System.getProperty("username");
        final String weightFacebookString = System.getProperty("weight-facebook");
        final String weightTransactionalString = System.getProperty("weight-transactional");
        final String weightCivilRegistryString = System.getProperty("weight-civil-registry");

        if (Strings.isNullOrEmpty(dateString)) {
            throw new IllegalArgumentException("Need date string on format yyyy-MM-dd");
        }

        final Date date = ThreadSafeDateFormat.FORMATTER_DAILY.parse(dateString);

        if (date == null) {
            throw new IllegalArgumentException("Need date string on format yyyy-MM-dd");
        }

        if (!Strings.isNullOrEmpty(weightFacebookString)) {
            weights[0] = Float.parseFloat(weightFacebookString);
        }

        if (!Strings.isNullOrEmpty(weightTransactionalString)) {
            weights[1] = Float.parseFloat(weightTransactionalString);
        }

        if (!Strings.isNullOrEmpty(weightCivilRegistryString)) {
            weights[2] = Float.parseFloat(weightCivilRegistryString);
        }

        weights[2] = 0f;

        Iterable<Float> set = Iterables.filter(Arrays.asList(weights), aFloat -> aFloat != -1);

        float sumNotSet = 1 - sum(set);
        float numNotSet = weights.length - Iterables.size(set);

        for (int i = 0; i < weights.length; i++) {
            if (weights[i] == -1) {
                weights[i] = sumNotSet / numNotSet;
            }
        }

        run(serviceContext, date, username);
    }

    private float sum(Iterable<Float> set) {
        float sum = 0.0f;
        for (float f : set) {
            sum += f;
        }
        return sum;
    }

    private void run(ServiceContext context, Date date, String username) {

        UserRepository userRepository = context.getRepository(UserRepository.class);

        User user = null;
        if (!Strings.isNullOrEmpty(username)) {
            user = userRepository.findOneByUsername(username);
        }

        AggregatedLocationEstimator estimator = new AggregatedLocationEstimator();

        FacebookBasedCityEstimator facebook = new FacebookBasedCityEstimator(context);

        estimator.addLocationEstimator(facebook, weights[INDEX_FB]);

        if (user != null) {
            List<CityLocationGuess> guesses = getLocationOfUser(context, estimator, user, date);
            printUserLocations(user, guesses);
        } else {
            List<String> users = userRepository.findAllUserIds();
            Random random = new Random();

            int max = users.size() > 20 ? 20 : users.size();
            for (int i = 0; i < max; i++) {
                String randomUserId = users.get(random.nextInt(users.size()));
                User randomUser = userRepository.findOne(randomUserId);

                List<CityLocationGuess> guesses = getLocationOfUser(context, estimator, randomUser, date);
                printUserLocations(randomUser, guesses);
            }
        }
    }

    private List<CityLocationGuess> getLocationOfUser(ServiceContext context,
            AggregatedLocationEstimator estimator, User user, Date date) {

        TransactionDao transactionRepository = context.getDao(TransactionDao.class);
        MerchantRepository merchantRepository = context.getRepository(MerchantRepository.class);
        List<Transaction> transactions = transactionRepository.findAllByUserId(user.getId());
        
        Iterable<Merchant> merchantsWithCity = Iterables.filter(merchantRepository.findAll(), Predicates.MERCHANT_HAS_CITY);
        Map<String, Merchant> merchantsById = Maps.uniqueIndex(merchantsWithCity, Merchant::getId);
        Map<String, String> merchantCityByMerchantId = Maps.transformValues(merchantsById, Merchant::getCity);
        
        TransactionBasedCityEstimator transactional = new TransactionBasedCityEstimator(merchantCityByMerchantId, transactions);

        estimator.addLocationEstimator(transactional, weights[INDEX_T]);

        List<CityLocationGuess> guesses = estimator.getLocationProbabilities(user, date);

        estimator.removeLocationEstimator(transactional);

        return guesses;
    }

    private void printUserLocations(User user, List<CityLocationGuess> guesses) {
        log.info(user.getId() + ":");
        List<CityLocationGuess> sorted = LOCATION_GUESS_PROBABILITY.sortedCopy(guesses);
        log.info(getLocationGuessesString(sorted));
    }

    private String getLocationGuessesString(List<CityLocationGuess> guesses) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (CityLocationGuess guess : guesses) {
            sb.append(guess.getCity() + ": " + guess.getProbability() + ", ");
        }
        sb.append(" ]");
        return sb.toString();
    }

    private static final Ordering<LocationGuess> LOCATION_GUESS_PROBABILITY = new Ordering<LocationGuess>() {
        @Override
        public int compare(@Nullable LocationGuess g1, @Nullable LocationGuess g2) {
            return -1 * Float.compare(g1.getProbability(), g2.getProbability());
        }
    };
}
