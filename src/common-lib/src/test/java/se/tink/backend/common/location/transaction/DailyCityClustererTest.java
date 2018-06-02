package se.tink.backend.common.location.transaction;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.common.location.LocationTestUtils.createMerchant;
import static se.tink.backend.common.location.LocationTestUtils.createTransaction;
import static se.tink.backend.common.location.LocationTestUtils.date;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import se.tink.backend.common.location.LocationTestUtils;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.guavaimpl.Predicates;

import com.google.common.collect.Lists;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class DailyCityClustererTest {

    MerchantRepository merchantRepository;
    List<Transaction> allTransactions;
    DailyCityClusterer clusterer;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        merchantRepository = mock(MerchantRepository.class);
        when(merchantRepository.findAll(any(Iterable.class))).thenReturn(createAllMerchants());
        when(merchantRepository.findAll()).thenReturn(createAllMerchants());

        allTransactions = createAllTransactions();

        Iterable<Merchant> merchantsWithCity = Iterables.filter(merchantRepository.findAll(), Predicates.MERCHANT_HAS_CITY);
        Map<String, Merchant> merchantsById = Maps.uniqueIndex(merchantsWithCity, Merchant::getId);
        Map<String, String> merchantCitysById = Maps.transformValues(merchantsById, Merchant::getCity);

        clusterer = new DailyCityClusterer(merchantCitysById);
    }

    @Test
    public void testJan1Radius1() throws Exception {
        List<DailyCityExistence> existences = LocationTestUtils.ORDERING_EXISTENCE.sortedCopy(clusterer.transactionsPerDayPerCity(allTransactions, date("2015-01-01"), 1));

        assertEquals(3, existences.size());
        verifyDailyCityExistence(existences.get(0), "2014-12-31", "Stockholm", 1);
        verifyDailyCityExistence(existences.get(1), "2015-01-01", "Stockholm", 3);
        verifyDailyCityExistence(existences.get(2), "2015-01-02", "Stockholm", 2);
    }

    @Test
     public void testJan1Radius2() throws Exception {
        List<DailyCityExistence> existences = LocationTestUtils.ORDERING_EXISTENCE.sortedCopy(clusterer.transactionsPerDayPerCity(allTransactions, date("2015-01-01"), 2));

        assertEquals(5, existences.size());
        verifyDailyCityExistence(existences.get(0), "2014-12-30", "Stockholm", 1);
        verifyDailyCityExistence(existences.get(1), "2014-12-31", "Stockholm", 1);
        verifyDailyCityExistence(existences.get(2), "2015-01-01", "Stockholm", 3);
        verifyDailyCityExistence(existences.get(3), "2015-01-02", "Stockholm", 2);
        verifyDailyCityExistence(existences.get(4), "2015-01-03", "Malmö", 2);
    }

    @Test
     public void testDec31Radius2NoTransactionsBefore() throws Exception {
        List<DailyCityExistence> existences = LocationTestUtils.ORDERING_EXISTENCE.sortedCopy(clusterer.transactionsPerDayPerCity(allTransactions, date("2014-12-31"), 2));

        assertEquals(4, existences.size());
        verifyDailyCityExistence(existences.get(0), "2014-12-30", "Stockholm", 1);
        verifyDailyCityExistence(existences.get(1), "2014-12-31", "Stockholm", 1);
        verifyDailyCityExistence(existences.get(2), "2015-01-01", "Stockholm", 3);
        verifyDailyCityExistence(existences.get(3), "2015-01-02", "Stockholm", 2);
    }

    @Test
    public void testJan3Radius1() throws Exception {
        List<DailyCityExistence> existences = LocationTestUtils.ORDERING_EXISTENCE.sortedCopy(clusterer.transactionsPerDayPerCity(allTransactions, date("2015-01-03"), 1));

        assertEquals(4, existences.size());
        verifyDailyCityExistence(existences.get(0), "2015-01-02", "Stockholm", 2);
        verifyDailyCityExistence(existences.get(1), "2015-01-03", "Malmö", 2);
        verifyDailyCityExistence(existences.get(2), "2015-01-04", "Malmö", 2);
        verifyDailyCityExistence(existences.get(3), "2015-01-04", "Stockholm", 1);
    }

    @Test
    public void testJan6Radius2NoTransactionsAfter() throws Exception {
        List<DailyCityExistence> existences = LocationTestUtils.ORDERING_EXISTENCE.sortedCopy(clusterer.transactionsPerDayPerCity(allTransactions, date("2015-01-06"), 2));

        assertEquals(4, existences.size());
        verifyDailyCityExistence(existences.get(0), "2015-01-04", "Malmö", 2);
        verifyDailyCityExistence(existences.get(1), "2015-01-04", "Stockholm", 1);
        verifyDailyCityExistence(existences.get(2), "2015-01-05", "Stockholm", 1);
        verifyDailyCityExistence(existences.get(3), "2015-01-06", "Stockholm", 1);
    }

    @Test
    public void testJan8Radius2Gap() throws Exception {
        List<DailyCityExistence> existences = LocationTestUtils.ORDERING_EXISTENCE.sortedCopy(clusterer.transactionsPerDayPerCity(allTransactions, date("2015-01-08"), 2));

        assertEquals(2, existences.size());
        verifyDailyCityExistence(existences.get(0), "2015-01-06", "Stockholm", 1);
        verifyDailyCityExistence(existences.get(1), "2015-01-09", "Stockholm", 1);
    }

    private void verifyDailyCityExistence (DailyCityExistence e, String date, String city, int num) {
        assertEquals(e.getDateString(), date);
        assertEquals(e.getCity(), city);
        assertEquals(e.getNumTransactions(), num);
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

        return transactions;
    }
}
