package se.tink.backend.combined.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.core.Category;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.SearchResult;
import se.tink.backend.core.StringDoublePair;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.rpc.SearchResponse;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;

import java.util.List;
import java.util.Locale;

/**
 * TODO this is a unit test
 */
public class SearchServiceIntegrationTest extends AbstractServiceIntegrationTest {
    protected static ObjectMapper mapper = new ObjectMapper();
    User user;

    static {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private CategoryConfiguration categoryConfiguration;

    @Before
    public void setUp() throws Exception {
        user = registerTestUserWithDemoCredentialsAndData("anv1ud");
        categoryConfiguration = serviceContext.getCategoryConfiguration();
    }

    @After
    public void tearDown() throws Exception {
        deleteUser(user);
    }

    private void assertThatSwedishTranslationsAreConfigured(User user) {
        // Verifies that the swedish translations are correctly configured

        final List<Category> categories = serviceFactory.getCategoryService().list(user, null);
        Category miscExpensedCategory = Iterables.find(categories,
                c -> c.getCode().equals(categoryConfiguration.getExpenseUnknownCode()));

        Assert.assertEquals("Okategoriserat", miscExpensedCategory.getSecondaryName());
        Assert.assertEquals("Utgifter", miscExpensedCategory.getTypeName());
        Assert.assertEquals("Övrigt", miscExpensedCategory.getPrimaryName());
    }

    public List<SearchResult> search(User user, String queryString) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQueryString(queryString);
        searchQuery.setLimit(20);

        SearchResponse searchResponse = serviceFactory.getSearchService().searchQuery(user, searchQuery);

        try {
            System.out.println(mapper.writeValueAsString(searchResponse));
        } catch (JsonProcessingException e) {
            // NOOP.
        }

        return searchResponse.getResults();
    }

    @Test
    public void testFuzzyAmountFilterSearch() throws Exception {
        int amount = 4500;

        SearchQuery q1 = new SearchQuery();
        q1.setQueryString("around " + String.valueOf(amount));
        q1.setOffset(0);
        q1.setLimit(10);

        SearchResponse r1 = serviceFactory.getSearchService().searchQuery(user, q1);

        List<SearchResult> results = r1.getResults();
        for (SearchResult result : results) {
            Assert.assertTrue(
                    "Failed on " + result.getTransaction().getAmount(),
                    Math.abs(result.getTransaction().getAmount()) > (amount - (amount * 0.05))
                            && Math.abs(result.getTransaction().getAmount()) < (amount + (amount * 0.05)));
        }
    }

    @Test
    public void testWildCardSearch() throws Exception {

        SearchQuery q1 = new SearchQuery();
        q1.setQueryString("BOQUE");
        q1.setOffset(0);
        q1.setLimit(10);

        SearchResponse r1 = serviceFactory.getSearchService().searchQuery(user, q1);

        List<SearchResult> results = r1.getResults();
        for (SearchResult result : results) {
            Assert.assertTrue("Failed on " + result.getTransaction().getDescription(), result.getTransaction()
                    .getDescription().toLowerCase().startsWith("boque"));
            Assert.assertTrue("Failed on " + result.getTransaction().getDescription(), result.getTransaction()
                    .getDescription().length() > 5);
        }
    }

    @Test
    public void testWildCardWithAmountSearch() throws Exception {

        SearchQuery q1 = new SearchQuery();
        q1.setQueryString("BOQUE over 150");
        q1.setOffset(0);
        q1.setLimit(10);

        SearchResponse r1 = serviceFactory.getSearchService().searchQuery(user, q1);

        List<SearchResult> results = r1.getResults();
        for (SearchResult result : results) {
            Assert.assertTrue("Failed on " + result.getTransaction().getDescription(), result.getTransaction()
                    .getDescription().toLowerCase().startsWith("boque"));
            Assert.assertTrue("Failed on " + result.getTransaction().getAmount(),
                    Math.abs(result.getTransaction().getAmount()) > 150);
        }
    }

    @Test
    public void testPrimaryNameCategorySearch() throws Exception {

        assertThatSwedishTranslationsAreConfigured(user);

        List<Category> categories = serviceFactory.getCategoryService().list(user, null);
        Category vacation = Iterables
                .find(categories, c -> c.getCode().equals(categoryConfiguration.getVacationCode()));

        ImmutableMap<String, Category> categoriesById = Maps.uniqueIndex(categories, Category::getId);

        Assert.assertTrue(categoriesById.size() > 0);

        SearchQuery q1 = new SearchQuery();
        q1.setQueryString("Fritid");
        q1.setOffset(0);
        q1.setLimit(10);

        SearchResponse r1 = serviceFactory.getSearchService().searchQuery(user, q1);

        List<SearchResult> results = r1.getResults();
        for (SearchResult result : results) {
            Assert.assertTrue("Failed on " + result.getTransaction().getDescription(),
                    categoriesById.get(result.getTransaction().getCategoryId()).getParent()
                            .equals(vacation.getParent()));
        }
    }

    @Test
    public void testWeekAroundNewYearOnSearch() throws Exception {
        // Create a user with data around new year
        user = registerTestUserWithDemoCredentialsAndData("anv25");

        SearchQuery q2 = new SearchQuery();
        q2.setQueryString("vecka 3");
        q2.setLimit(10000);

        SearchResponse fullResult = serviceFactory.getSearchService().searchQuery(user, q2);

        // Verifies that the amounts are correct for each week around new year
        for (StringDoublePair pair : fullResult.getPeriodAmounts()) {
            switch (pair.getKey()) {
            case "2014:51":
                Assert.assertEquals(-1426.72, pair.getValue(), 0.0001);
                break;
            case "2014:52":
                Assert.assertEquals(-1056.90, pair.getValue(), 0.0001);
                break;
            case "2015:01":
                Assert.assertEquals(-742.98, pair.getValue(), 0.0001);
                break;
            case "2015:02":
                Assert.assertEquals(-433.56, pair.getValue(), 0.0001);
                break;

            }

        }

    }

    @Test
    public void testStatisticsDataOnSearch() throws Exception {
        user = registerTestUserWithDemoCredentialsAndData("201212121212");

        // Week
        final Locale locale = Catalog.getLocale(user.getProfile().getLocale());
        final ThreadSafeDateFormat weeklyFormatter = ThreadSafeDateFormat.FORMATTER_WEEKLY
                .toBuilder().setLocale(locale).build();

        final Function<Transaction, String> weekFunction = t -> weeklyFormatter.format(t.getDate());
        statisticsDataOnPeriodSearch(user, "mat förra veckan", weekFunction);

        //Month

        Function<Transaction, String> monthFunction = t -> ThreadSafeDateFormat.FORMATTER_MONTHLY.format(t.getDate());
        statisticsDataOnPeriodSearch(user, "mat förra månaden", monthFunction);

        // Year

        Function<Transaction, String> yearFunction = t -> ThreadSafeDateFormat.FORMATTER_YEARLY.format(t.getDate());
        statisticsDataOnPeriodSearch(user, "mat 2013", yearFunction);

        // Day

        Function<Transaction, String> dayFunction = t -> ThreadSafeDateFormat.FORMATTER_DAILY.format(t.getDate());
        statisticsDataOnPeriodSearch(user, "mat idag", dayFunction);

        // Default

        Function<Transaction, String> monthFunctionAdjusted = t -> UserProfile.ProfileDateUtils
                .getMonthPeriod(t.getDate(), user.getProfile());

        statisticsDataOnPeriodSearch(user, "mat", monthFunctionAdjusted);
    }

    private void statisticsDataOnPeriodSearch(User user, String query, Function<Transaction, String> periodFunction) {
        SearchQuery q1 = new SearchQuery();
        q1.setQueryString(query);
        SearchResponse result = serviceFactory.getSearchService().searchQuery(user, q1);

        // Assert there are hits.
        List<StringDoublePair> statistics = result.getPeriodAmounts();
        Assert.assertTrue("No statistics for query", statistics.size() > 0);

        // Assert sum is correct.

        SearchQuery q2 = new SearchQuery();
        q2.setQueryString("mat");
        q2.setLimit(10000);

        SearchResponse fullResult = serviceFactory.getSearchService().searchQuery(user, q2);

        ImmutableListMultimap<String, Transaction> transactionByPeriod = sumTransactionForPeriod(fullResult,
                periodFunction);

        log.info("Query: " + query);

        for (StringDoublePair pair : statistics) {
            log.info("\t" + pair.getKey() + "\t" + pair.getValue());

            List<Transaction> transactions = transactionByPeriod.get(pair.getKey());

            if (pair.getValue() == 0) {
                Assert.assertTrue(transactions.size() == 0);
            } else {
                Assert.assertTrue(transactions.size() > 0);
            }

            double sum = 0;
            for (Transaction t : transactions) {
                sum += t.getAmount();
            }
            Assert.assertEquals("Statistics sum is not correct for period " + pair.getKey() + " for query " + query,
                    Math.round(sum), Math.round((pair.getValue())), 0);
        }
    }

    private ImmutableListMultimap<String, Transaction> sumTransactionForPeriod(SearchResponse r1,
            Function<Transaction, String> periodFunction) {
        return Multimaps.index(Iterables.transform(r1.getResults(), SearchResult::getTransaction), periodFunction);
    }
}
