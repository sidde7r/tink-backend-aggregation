package se.tink.backend.combined.integration;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.text.ParseException;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.common.repository.cassandra.UserLocationRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Category;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.MerchantCluster;
import se.tink.backend.core.MerchantSources;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.SearchResult;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserLocation;
import se.tink.backend.rpc.MerchantQuery;
import se.tink.backend.rpc.MerchantQueryResponse;
import se.tink.backend.rpc.MerchantSkipRequest;
import se.tink.backend.rpc.MerchantizeTransactionsRequest;
import se.tink.backend.rpc.SearchResponse;
import se.tink.backend.rpc.SuggestMerchantizeRequest;
import se.tink.backend.rpc.SuggestMerchantizeResponse;
import se.tink.backend.utils.StringUtils;

/**
 * TODO this is a unit test
 */
public class MerchantServiceIntegrationTest extends AbstractServiceIntegrationTest {

    private String restaurantCategory;

    @Before
    public void setUp() throws Exception {
        restaurantCategory = serviceContext.getCategoryConfiguration().getRestaurantsCode();
    }

    @Test
    public void testQueryMerchant() throws ParseException {

        User user = registerUser(randomUsername(), "testing", createUserProfile());
        MerchantQuery request = new MerchantQuery();

        request.setQueryString("McDonalds Kungs");
        MerchantQueryResponse result = serviceFactory.getMerchantService().query(user, request);

        Assert.assertTrue("No hits", result.getMerchants().size() > 0);
    }

    @Test
    public void testGetMerchant() throws ParseException {
        User user = registerUser(randomUsername(), "testing", createUserProfile());
        MerchantQuery request = new MerchantQuery();

        request.setQueryString("McDonalds Kungs");
        MerchantQueryResponse result = serviceFactory.getMerchantService().query(user, request);

        Assert.assertTrue("No hits", result.getMerchants().size() > 0);

        Merchant merchantQuery = result.getMerchants().get(0);

        serviceContext.getRepository(MerchantRepository.class).save(merchantQuery);

        Merchant merchantGet = serviceFactory.getMerchantService().get(user, merchantQuery.getId());

        assertMerchantsEqual(merchantQuery, merchantGet);
    }

    /**
     * Verifies that we can merchantize a transaction with a merchant
     * that we picked up from google
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void testMerchantizeWithNewMerchantFromGoogle() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        // Find transaction to merchantize.
        Transaction transaction = getRandomTransaction(user);

        // Find merchant for transaction.
        MerchantQuery merchantQuery = new MerchantQuery();
        merchantQuery.setQueryString("Vapiano");

        MerchantQueryResponse merchants = serviceFactory.getMerchantService().query(user, merchantQuery);
        Assert.assertTrue(merchants.getMerchants().size() > 0);
        Merchant merchant = merchants.getMerchants().get(0);

        // Merchantize transaction.

        MerchantizeTransactionsRequest merchantizeRequest = new MerchantizeTransactionsRequest();
        merchantizeRequest.setMerchant(merchant);
        merchantizeRequest.setTransactionIds(Lists.newArrayList(transaction.getId()));

        Merchant merchantCreated = serviceFactory.getMerchantService().merchantize(user, merchantizeRequest);

        // Verify transaction has merchant.
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setLimit(0);
        searchQuery.setTransactionId(transaction.getId());
        SearchResponse searchRsp2 = serviceFactory.getSearchService().searchQuery(user, searchQuery);
        Assert.assertTrue(searchRsp2.getResults().size() > 0);

        Assert.assertEquals(merchantCreated.getId(), searchRsp2.getResults().get(0).getTransaction().getMerchantId());

        // Verify saved merchant has name and address from Google query.
        Assert.assertEquals(merchantCreated.getName(), merchant.getName());

        Assert.assertEquals(merchantCreated.getReference(), merchant.getReference());

        // The formatted address can/will differ between the created merchant and the autocompleted merchant
        // since we don't get street number and zip code from google
        Assert.assertNotNull(merchantCreated.getFormattedAddress());
        Assert.assertNotNull(merchant.getFormattedAddress());

        // Verify that source is google and visible for all users
        Assert.assertEquals(MerchantSources.GOOGLE, merchant.getSource());
        Assert.assertNull(merchant.getVisibleToUsers());

    }

    @Ignore
    @Test
    public void testMerchantizeWithNewMerchantWithAddressFromGoogle() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        // Find transaction to merchantize.
        Transaction transaction = getRandomTransaction(user);

        // Find address to new Merchant.
        MerchantQuery merchantQuery = new MerchantQuery();
        merchantQuery.setQueryString("Hornsgatan 69");
        MerchantQueryResponse addressListRsp = serviceFactory.getMerchantService().address(user, merchantQuery);

        Assert.assertTrue(addressListRsp.getMerchants().size() > 0);
        Merchant merchant = addressListRsp.getMerchants().get(0);

        Merchant merchantNew = new Merchant();
        String merchantName = "Jerniabutiken på Hornsgatan";
        merchantNew.setName(merchantName);
        merchantNew.setReference(merchant.getReference());

        // Merchantize transaction.

        MerchantizeTransactionsRequest merchantizeRequest = new MerchantizeTransactionsRequest();
        merchantizeRequest.setMerchant(merchantNew);
        merchantizeRequest.setTransactionIds(Lists.newArrayList(transaction.getId()));

        Merchant merchantCreated = serviceFactory.getMerchantService().merchantize(user, merchantizeRequest);

        // Verify saved merchant has name and address from Google query.
        Assert.assertNotNull(merchantCreated.getId());
        Assert.assertEquals(merchantName, merchantCreated.getName());
        Assert.assertNotNull(merchantCreated.getFormattedAddress());

        Assert.assertEquals(merchantCreated.getReference(), merchant.getReference());

        // Verify that the new merchant only is visible for the user that created it
        Assert.assertNotNull(merchantCreated.getVisibleToUsers());
        Assert.assertEquals(MerchantSources.MANUALLY, merchantCreated.getSource());
        Assert.assertTrue(merchantCreated.getVisibleToUsers().contains(user.getId()));

    }

    @Ignore
    @Test
    public void testMerchantizeWithNewMerchantOnlyName() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        // Find transaction to merchantize.
        List<Transaction> transactions = search(user, "Järnia", 1);

        Assert.assertTrue(transactions.size() > 0);

        Transaction transaction = transactions.get(0);

        String name = "Jerniabutiken på Hornsgatan";

        Merchant merchantNew = new Merchant();
        merchantNew.setName(name);

        // Merchantize transaction.

        MerchantizeTransactionsRequest merchantizeRequest = new MerchantizeTransactionsRequest();
        merchantizeRequest.setMerchant(merchantNew);
        merchantizeRequest.setTransactionIds(Lists.newArrayList(transaction.getId()));

        Merchant merchantCreated = serviceFactory.getMerchantService().merchantize(user, merchantizeRequest);

        // Verify saved merchant has name only.

        Assert.assertNotNull(merchantCreated.getId());
        Assert.assertEquals(name, merchantCreated.getName());
        Assert.assertEquals(name, merchantNew.getName());
        Assert.assertNull(merchantCreated.getReference());
        Assert.assertNull(merchantCreated.getFormattedAddress());

        Assert.assertEquals(MerchantSources.MANUALLY, merchantCreated.getSource());
        Assert.assertNotNull(merchantCreated.getVisibleToUsers());
        Assert.assertTrue(merchantCreated.getVisibleToUsers().contains(user.getId()));
    }

    @Ignore
    @Test
    public void testMerchantizeWithAlreadyExistingMerchant() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        String id = StringUtils.generateUUID();
        String name = "This is a merchant";
        String address = "This is an address";

        Merchant dummyMerchant = new Merchant();
        dummyMerchant.setId(id);
        dummyMerchant.setName(name);
        dummyMerchant.setFormattedAddress(address);

        // Add a dummy merchant
        serviceContext.getRepository(MerchantRepository.class).saveAndIndex(dummyMerchant);

        Transaction transaction = getRandomTransaction(user);

        // Merchantize transaction.

        MerchantizeTransactionsRequest merchantizeRequest = new MerchantizeTransactionsRequest();
        merchantizeRequest.setMerchant(dummyMerchant);
        merchantizeRequest.setTransactionIds(Lists.newArrayList(transaction.getId()));

        Merchant merchantCreated = serviceFactory.getMerchantService().merchantize(user, merchantizeRequest);

        // Verify saved merchant has name only.

        Assert.assertEquals(id, merchantCreated.getId());
        Assert.assertEquals(name, merchantCreated.getName());
        Assert.assertEquals(address, merchantCreated.getFormattedAddress());
    }

    @Ignore
    @Test
    public void testMerchantizeWithNewMerchantNameAndAddress() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        // Find transaction to merchantize.
        List<Transaction> transactions = search(user, "grill", 1);

        Assert.assertTrue(transactions.size() > 0);

        Transaction transaction = transactions.get(0);

        String name = "Annedals Grill";
        String address = "Pippi Långstrumps gata 30";

        Merchant merchantNew = new Merchant();
        merchantNew.setName(name);
        merchantNew.setFormattedAddress(address);

        // Merchantize transaction.

        MerchantizeTransactionsRequest merchantizeRequest = new MerchantizeTransactionsRequest();
        merchantizeRequest.setMerchant(merchantNew);
        merchantizeRequest.setTransactionIds(Lists.newArrayList(transaction.getId()));

        Merchant merchantCreated = serviceFactory.getMerchantService().merchantize(user, merchantizeRequest);

        // Verify saved merchant has name only.

        Assert.assertNotNull(merchantCreated.getId());
        Assert.assertEquals(name, merchantCreated.getName());
        Assert.assertEquals(name, merchantNew.getName());
        Assert.assertEquals(address, merchantNew.getFormattedAddress());
        Assert.assertEquals(address, merchantCreated.getFormattedAddress());
        Assert.assertNull(merchantCreated.getReference());
    }

    @Ignore
    @Test
    public void testMerchantificationSuggest() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        String restaurantId = getRestaurantsCategoryId(user);

        SuggestMerchantizeRequest request = new SuggestMerchantizeRequest();
        request.setCategoryId(restaurantId);
        request.setNumberOfClusters(4);

        SuggestMerchantizeResponse response = serviceFactory.getMerchantService().suggest(user, request);

        Assert.assertNotNull(response.getClusters());
        Assert.assertEquals(4, response.getClusters().size(), 0);
        Assert.assertEquals(restaurantId, response.getClusterCategoryId());
        Assert.assertTrue(response.getMerchantificationImprovement() > 0);

        double improvmentSum = 0;

        for (MerchantCluster cluster : response.getClusters()) {
            Assert.assertNotNull(cluster.getDescription());
            Assert.assertNotNull(cluster.getTransactions());
            Assert.assertTrue(cluster.getMerchantificationImprovement() > 0);

            improvmentSum += cluster.getMerchantificationImprovement();

            for (Transaction t : cluster.getTransactions()) {
                Assert.assertEquals(restaurantId, t.getCategoryId());
                Assert.assertNull(t.getMerchantId());
            }
        }
        Assert.assertEquals(improvmentSum, response.getMerchantificationImprovement(), 0);
    }

    private void assertMerchantsEqual(Merchant merchantExpected, Merchant merchantActual) {
        Assert.assertEquals(merchantExpected.getName(), merchantActual.getName());
        Assert.assertEquals(merchantExpected.getFormattedAddress(), merchantActual.getFormattedAddress());
    }

    @Ignore
    @Test
    public void testMerchantizeSeveralTransactions() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        List<Transaction> searchResult = search(user, "COOP", 3);
        Assert.assertTrue(searchResult.size() > 0);

        Transaction transaction1 = searchResult.get(0);
        Transaction transaction2 = searchResult.get(1);

        Merchant merchantNew = new Merchant();
        merchantNew.setName("COOP i Bromma");

        // Merchantize transaction.

        MerchantizeTransactionsRequest merchantizeRequest = new MerchantizeTransactionsRequest();
        merchantizeRequest.setMerchant(merchantNew);
        merchantizeRequest.setTransactionIds(Lists.newArrayList(transaction1.getId(), transaction2.getId()));

        serviceFactory.getMerchantService().merchantize(user, merchantizeRequest);

        // Wait for results to be indexed
        Thread.sleep(3000);

        List<Transaction> searchResult2 = search(user, "COOP", 10000);

        Transaction updatedTransaction1 = null;
        Transaction updatedTransaction2 = null;

        for (Transaction t : searchResult2) {
            if (t.getId().equals(transaction1.getId())) {
                updatedTransaction1 = t;
            }

            if (t.getId().equals(transaction2.getId())) {
                updatedTransaction2 = t;
            }

        }

        Assert.assertNotNull(updatedTransaction1);
        Assert.assertEquals("COOP i Bromma", updatedTransaction1.getDescription());
        Assert.assertTrue(updatedTransaction1.isUserModifiedLocation());

        Assert.assertNotNull(updatedTransaction2);
        Assert.assertEquals("COOP i Bromma", updatedTransaction2.getDescription());
        Assert.assertTrue(updatedTransaction2.isUserModifiedLocation());
    }

    /**
     * Verifies that users locations are taken into consideration when the user search for merchants
     * (Test is depending on that there is a OKQ8 petrol station is Malmö)
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void shouldIncludeUserLocationsInSearchForPetrolStationInMalmö() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        List<Transaction> transactions = search(user, "OKQ8", 1);

        Assert.assertTrue(transactions.size() > 0);

        Transaction transaction = transactions.get(0);

        UserLocationRepository userLocationRepository = serviceContext.getRepository(UserLocationRepository.class);

        // Fake a location for the transaction date in Malmö
        UserLocation location = getMalmoLocation();
        location.setUserId(UUIDUtils.fromTinkUUID(user.getId()));
        location.setId(UUIDs.startOf(transaction.getDate().getTime()));
        location.setDate(transaction.getDate());

        userLocationRepository.save(location);

        MerchantQuery request = new MerchantQuery();
        request.setLimit(1);
        request.setQueryString("OKQ8");
        request.setTransactionId(transaction.getId());

        MerchantQueryResponse service = serviceFactory.getMerchantService().query(user, request);

        List<Merchant> merchants = service.getMerchants();

        // Malmö OKQ8 should be prioritized in the search result
        Assert.assertEquals(1, merchants.size());
        Assert.assertTrue(merchants.get(0).getFormattedAddress().contains("Malmö"));
    }

    /**
     * Verifies that users locations are taken into consideration when the user search for merchants
     * (Test is depending on that there is a OKQ8 petrol station is Linköping)
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void shouldIncludeUserLocationsInSearchForPetrolStationInLinköping() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        List<Transaction> transactions = search(user, "OKQ8", 1);

        Assert.assertTrue(transactions.size() > 0);

        Transaction transaction = transactions.get(0);

        UserLocationRepository userLocationRepository = serviceContext.getRepository(UserLocationRepository.class);

        // Fake a location for the transaction date in Linköping
        UserLocation location = getLinkopingLocation();
        location.setUserId(UUIDUtils.fromTinkUUID(user.getId()));
        location.setId(UUIDs.startOf(transaction.getDate().getTime()));
        location.setDate(transaction.getDate());

        userLocationRepository.save(location);

        MerchantQuery request = new MerchantQuery();
        request.setLimit(1);
        request.setQueryString("OKQ8");
        request.setTransactionId(transaction.getId());

        MerchantQueryResponse service = serviceFactory.getMerchantService().query(user, request);

        List<Merchant> merchants = service.getMerchants();

        // Linköping OKQ8 should be prioritized in the search result
        Assert.assertEquals(1, merchants.size());
        Assert.assertTrue(merchants.get(0).getFormattedAddress().contains("Linköping"));
    }

    /**
     * Verifies that users can search for merchants even if that they don't have any user locations in the system
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void shouldSearchWithOutUserLocation() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        List<Transaction> transactions = search(user, "OKQ8", 1);

        Assert.assertTrue(transactions.size() > 0);

        Transaction transaction = transactions.get(0);

        MerchantQuery request = new MerchantQuery();
        request.setLimit(1);
        request.setQueryString("OKQ8");
        request.setTransactionId(transaction.getId());

        MerchantQueryResponse service = serviceFactory.getMerchantService().query(user, request);

        List<Merchant> merchants = service.getMerchants();

        Assert.assertEquals(1, merchants.size());
    }

    /**
     * Verifies that we don't get any addresses for merchant lookup. (Only places/merchants).
     * Searching for "Riche" should not return Richtergatan in Gothenburg since it is an address
     *
     * @throws Exception
     */
    @Test
    public void shouldOnlyReturnEstablishmentsForMerchantLookup() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        MerchantQuery request = new MerchantQuery();
        request.setLimit(10);
        request.setQueryString("Riche");

        MerchantQueryResponse service = serviceFactory.getMerchantService().query(user, request);

        List<Merchant> merchants = service.getMerchants();

        Assert.assertTrue(merchants.size() > 0);

        for (Merchant m : merchants) {
            Assert.assertNotEquals("Should not be any merchant with this name", "Richertsgatan", m.getName());
        }
    }

    /**
     * Verifies that we don't get any places/establishments when we do a address lookup
     *
     * @throws Exception
     */
    @Test
    public void shouldOnlyReturnAddressesForAddressLookup() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        MerchantQuery request = new MerchantQuery();
        request.setLimit(10);
        request.setQueryString("Riche");

        MerchantQueryResponse service = serviceFactory.getMerchantService().address(user, request);

        List<Merchant> merchants = service.getMerchants();

        Assert.assertTrue(merchants.size() > 0);

        for (Merchant m : merchants) {

            Assert.assertFalse("Should not be any merchant with this name",
                    m.getFormattedAddress().toLowerCase().startsWith("restaurang riche"));
        }
    }

    @Test
    public void testSearchForLocalMerchants() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        String id = StringUtils.generateUUID();
        String name = "Some Weird Name Of a Merchant";
        String address = "This is an address";

        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setName(name);
        merchant.setFormattedAddress(address);
        merchant.setVisibleToUsers(Lists.newArrayList(user.getId()));
        merchant.setSource(MerchantSources.MANUALLY);

        // Add the merchant
        serviceContext.getRepository(MerchantRepository.class).saveAndIndex(merchant);

        // Find merchant for transaction.
        MerchantQuery merchantQuery = new MerchantQuery();
        merchantQuery.setQueryString(name);

        MerchantQueryResponse merchants = serviceFactory.getMerchantService().query(user, merchantQuery);
        Assert.assertTrue(merchants.getMerchants().size() > 0);
        Merchant result = merchants.getMerchants().get(0);

        Assert.assertEquals(id, result.getId());
        Assert.assertEquals(name, result.getName());
        Assert.assertEquals(address, result.getFormattedAddress());
        Assert.assertTrue(result.getVisibleToUsersSerialized().contains(user.getId()));
    }

    private Transaction getRandomTransaction(User u) {
        List<Transaction> transactions = search(u, "", 1);
        Assert.assertEquals(1, transactions.size());

        return transactions.get(0);
    }

    private List<Transaction> search(User user, String query, int limit) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setLimit(limit);
        searchQuery.setQueryString(query);

        SearchResponse searchRsp = serviceFactory.getSearchService().searchQuery(user, searchQuery);

        List<Transaction> result = Lists.newArrayList();

        for (SearchResult sr : searchRsp.getResults()) {
            result.add(sr.getTransaction());
        }

        return result;
    }

    /**
     * @return a location in Linköping
     */
    private UserLocation getLinkopingLocation() {
        UserLocation location = new UserLocation();
        location.setAccuracy(10);
        location.setLatitude(58.410807);
        location.setLongitude(15.621373);
        return location;
    }

    /**
     * @return a location in Malmö
     */
    private UserLocation getMalmoLocation() {
        UserLocation location = new UserLocation();
        location.setAccuracy(10);
        location.setLatitude(55.604981);
        location.setLongitude(13.003822);
        return location;
    }

    @Ignore
    @Test
    public void testMerchantificationSuggestWithSkippedTransactions() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        SuggestMerchantizeRequest request = new SuggestMerchantizeRequest();
        request.setCategoryId(getRestaurantsCategoryId(user));
        request.setNumberOfClusters(4);

        SuggestMerchantizeResponse response = serviceFactory.getMerchantService().suggest(user, request);

        List<String> transactionIds = Lists.newArrayList(Iterables
                .transform(response.getClusters().get(0).getTransactions(), Transaction::getId));

        MerchantSkipRequest skip = new MerchantSkipRequest();
        skip.setTransactionIds(transactionIds);

        // Skip the first cluster
        serviceFactory.getMerchantService().skip(user, skip);

        // Do the same suggest again
        SuggestMerchantizeResponse response2 = serviceFactory.getMerchantService().suggest(user, request);

        // Verify that we did not get the skipped cluster/transactions
        AssertSameClusterContent(response.getClusters().get(1), response2.getClusters().get(0));
        AssertSameClusterContent(response.getClusters().get(2), response2.getClusters().get(1));
        AssertSameClusterContent(response.getClusters().get(3), response2.getClusters().get(2));
    }

    private void AssertSameClusterContent(MerchantCluster cluster1, MerchantCluster cluster2) {
        Assert.assertEquals(cluster1.getDescription(), cluster2.getDescription());
        Assert.assertEquals(cluster1.getTransactions().size(), cluster2.getTransactions().size());

        outer:
        for (Transaction t1 : cluster1.getTransactions()) {
            for (Transaction t2 : cluster2.getTransactions()) {
                if (t1.getId().equals(t2.getId())) {
                    break outer;
                }
            }
            Assert.fail("Transactions does not match");
        }

    }

    private String getRestaurantsCategoryId(User user) {
        List<Category> categories = serviceFactory.getCategoryService().list(user, null);

        Category restaurants = Iterables.find(categories, c -> c.getCode().equals(restaurantCategory));

        return restaurants.getId();
    }

}
