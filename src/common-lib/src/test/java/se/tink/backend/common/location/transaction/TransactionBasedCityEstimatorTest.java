package se.tink.backend.common.location.transaction;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.common.location.LocationTestUtils.createMerchant;
import static se.tink.backend.common.location.LocationTestUtils.createTransaction;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.location.CityLocationGuess;
import se.tink.backend.common.location.LocationGuessType;
import se.tink.backend.common.location.LocationResolution;
import se.tink.backend.common.location.LocationTestUtils;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.guavaimpl.Predicates;

import com.google.common.collect.Lists;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class TransactionBasedCityEstimatorTest {

    MerchantRepository merchantRepository;
    List<Transaction> allTransactions;
    TransactionBasedCityEstimator estimator;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        merchantRepository = mock(MerchantRepository.class);
        when(merchantRepository.findAll(any(Iterable.class))).thenReturn(createAllMerchants());
        when(merchantRepository.findAll()).thenReturn(createAllMerchants());

        allTransactions = createAllTransactions();
        
        Iterable<Merchant> merchantsWithCity = Iterables.filter(merchantRepository.findAll(), Predicates.MERCHANT_HAS_CITY);
        Map<String, Merchant> merchantsById = Maps.uniqueIndex(merchantsWithCity, Merchant::getId);
        Map<String, String> merchantCityByMerchantId = Maps.transformValues(merchantsById, Merchant::getCity);

        ServiceContext serviceContext = mock(ServiceContext.class);
        when(serviceContext.getRepository(MerchantRepository.class)).thenReturn(merchantRepository);

        estimator = new TransactionBasedCityEstimator(merchantCityByMerchantId, allTransactions);
    }

    @Test
    public void testHas100pStockholmOnTarget() {
        List<CityLocationGuess> guesses = estimator.estimate(null, LocationTestUtils.date("2015-01-01"), 0);

        assertEquals(1, guesses.size());
        verifyDailyCityExistence(guesses.get(0), "Stockholm", 1f);
    }

    @Test
    public void testHas100pMalmoOnTarget() {
        List<CityLocationGuess> guesses = estimator.estimate(null, LocationTestUtils.date("2015-01-03"), 0);

        assertEquals(1, guesses.size());
        verifyDailyCityExistence(guesses.get(0), "Malmö", 1f);
    }

    @Test
    public void testHas100pStockholmOnTargetWithOtherInRadius() {
        List<CityLocationGuess> guesses = estimator.estimate(null, LocationTestUtils.date("2015-01-01"), 5);

        assertEquals(1, guesses.size());
        verifyDailyCityExistence(guesses.get(0), "Stockholm", 1f);
    }

    @Test
    public void testHas100pStockholmInRadius() {
        List<CityLocationGuess> guesses = estimator.estimate(null, LocationTestUtils.date("2015-01-08"), 2);

        assertEquals(1, guesses.size());
        verifyDailyCityExistence(guesses.get(0), "Stockholm", 1f);
    }

    @Test
    public void testHas33pStockholm67MalmoOnTarget() {
        List<CityLocationGuess> guesses = LocationTestUtils.ORDERING_CITY_GUESS.sortedCopy(estimator.estimate(null, LocationTestUtils.date("2015-01-04"), 0));

        assertEquals(2, guesses.size());
        verifyDailyCityExistence(guesses.get(0), "Malmö", 2f/3f);
        verifyDailyCityExistence(guesses.get(1), "Stockholm", 1f/3f);
    }


    @Test
    public void testHas50pStockholm50pMalmoInRadius() {
        List<CityLocationGuess> guesses = LocationTestUtils.ORDERING_CITY_GUESS.sortedCopy(estimator.estimate(null, LocationTestUtils.date("2015-02-04"), 3));

        assertEquals(2, guesses.size());
        verifyDailyCityExistence(guesses.get(0), "Malmö", 1f/2f);
        verifyDailyCityExistence(guesses.get(1), "Stockholm", 1f/2f);
    }

    @Test
    public void testHas75pStockholm25pMalmoInRadius() {
        List<CityLocationGuess> guesses = LocationTestUtils.ORDERING_CITY_GUESS.sortedCopy(estimator.estimate(null, LocationTestUtils.date("2015-03-04"), 3));

        assertEquals(2, guesses.size());
        verifyDailyCityExistence(guesses.get(0), "Malmö", 1f/4f);
        verifyDailyCityExistence(guesses.get(1), "Stockholm", 3f/4f);
    }



    private void verifyDailyCityExistence (CityLocationGuess g, String city, float probability) {
        Assert.assertEquals(LocationResolution.CITY, g.getResolution());
        Assert.assertEquals(LocationGuessType.TRANSACTIONAL, g.getType());

        assertEquals(city, g.getCity());
        assertEquals(probability, g.getProbability(), 0.00001f);
    }

    private static List<Merchant> createAllMerchants() {
        List<Merchant> merchants = Lists.newArrayList();
        merchants.add(createMerchant("Stockholm", "merchantStockholm1"));
        merchants.add(createMerchant("Stockholm", "merchantStockholm2"));
        merchants.add(createMerchant("Stockholm", "merchantStockholm3"));
        merchants.add(createMerchant("Stockholm", "merchantStockholm4"));
        merchants.add(createMerchant("Stockholm", "merchantStockholm5"));
        merchants.add(createMerchant("Stockholm", "merchantStockholm6"));
        merchants.add(createMerchant("Stockholm", "merchantStockholm9"));
        merchants.add(createMerchant("Malmö", "merchantMalmo1"));
        merchants.add(createMerchant("Malmö", "merchantMalmo2"));
        merchants.add(createMerchant("Malmö", "merchantMalmo3"));
        return merchants;
    }

    private static List<Transaction> createAllTransactions() throws Exception {
        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("2014-12-30 12:00", "merchantStockholm1"));
        transactions.add(createTransaction("2014-12-31 12:00", "merchantStockholm2"));
        transactions.add(createTransaction("2015-01-01 12:00", "merchantStockholm2"));
        transactions.add(createTransaction("2015-01-01 12:00", "merchantStockholm4"));
        transactions.add(createTransaction("2015-01-01 12:00", "merchantStockholm3"));
        transactions.add(createTransaction("2015-01-02 12:00", "merchantStockholm5"));
        transactions.add(createTransaction("2015-01-02 12:00", "merchantStockholm6"));
        transactions.add(createTransaction("2015-01-03 12:00", "merchantMalmo1"));
        transactions.add(createTransaction("2015-01-03 12:00", "merchantMalmo2"));
        transactions.add(createTransaction("2015-01-04 12:00", "merchantMalmo3"));
        transactions.add(createTransaction("2015-01-04 12:00", "merchantMalmo2"));
        transactions.add(createTransaction("2015-01-04 12:00", "merchantStockholm6"));
        transactions.add(createTransaction("2015-01-05 12:00", "merchantStockholm5"));
        transactions.add(createTransaction("2015-01-06 12:00", "merchantStockholm9"));
        transactions.add(createTransaction("2015-01-08 12:00", null));
        transactions.add(createTransaction("2015-01-08 12:00", null));
        transactions.add(createTransaction("2015-01-09 12:00", "merchantStockholm9"));

        transactions.add(createTransaction("2015-02-01 12:00", "merchantStockholm9"));
        transactions.add(createTransaction("2015-02-02 12:00", null));
        transactions.add(createTransaction("2015-02-03 12:00", null));
        transactions.add(createTransaction("2015-02-04 12:00", null));
        transactions.add(createTransaction("2015-02-05 12:00", null));
        transactions.add(createTransaction("2015-02-06 12:00", "merchantMalmo1"));

        transactions.add(createTransaction("2015-03-01 12:00", "merchantStockholm9"));
        transactions.add(createTransaction("2015-03-02 12:00", null));
        transactions.add(createTransaction("2015-03-03 12:00", null));
        transactions.add(createTransaction("2015-03-04 12:00", null));
        transactions.add(createTransaction("2015-03-06 12:00", "merchantStockholm9"));
        transactions.add(createTransaction("2015-03-06 12:00", "merchantMalmo1"));


        return transactions;
    }

}
