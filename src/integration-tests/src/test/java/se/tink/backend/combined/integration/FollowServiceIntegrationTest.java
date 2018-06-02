package se.tink.backend.combined.integration;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Credentials;
import se.tink.credentials.demo.DemoCredentials;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.StringDoublePair;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.FollowCriteria;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.core.follow.SavingsFollowCriteria;
import se.tink.backend.core.follow.SearchFollowCriteria;
import se.tink.backend.rpc.SearchResponse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

/**
 * TODO this is a unit test
 */
public class FollowServiceIntegrationTest extends AbstractServiceIntegrationTest {

    private String restaurantCategory;
    private String groceriesCategory;

    @Before
    public void setUp() {
        restaurantCategory = serviceContext.getCategoryConfiguration().getRestaurantsCode();
        groceriesCategory = serviceContext.getCategoryConfiguration().getGroceriesCode();
    }

    @Test
    public void testSearchFollowItems() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("anv2");

        SearchFollowCriteria followCriteria1 = new SearchFollowCriteria();
        followCriteria1.setQueryString("mat");
        followCriteria1.setTargetAmount(-500d);

        FollowItem followItem1a = new FollowItem();
        followItem1a.setName("Food");
        followItem1a.setType(FollowTypes.SEARCH);
        followItem1a.setCriteria(SerializationUtils.serializeToString(followCriteria1));

        serviceFactory.getFollowService().create(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), followItem1a);

        List<FollowItem> followItems = serviceFactory.getFollowService().list(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), null);

        final UserProfile profile = serviceFactory.getUserService().getProfile(user);
        final String period = "2014-04";

        for (FollowItem followItem : followItems) {
            FollowItem details = serviceFactory.getFollowService().get(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), followItem.getId(), period);

            SearchFollowCriteria criteria = SerializationUtils.deserializeFromString(details.getCriteria(),
                    SearchFollowCriteria.class);

            SearchQuery query = new SearchQuery();
            query.setQueryString(criteria.getQueryString());
            query.setLimit(500);

            SearchResponse searchResults = serviceFactory.getSearchService().searchQuery(user, query);

            Iterable<Transaction> transactions = Iterables.filter(
                    Iterables.transform(searchResults.getResults(), sr -> (sr.getTransaction())),
                    t -> (Objects.equal(UserProfile.ProfileDateUtils.getMonthPeriod(t.getDate(), profile), period)));

            Assert.assertEquals(Iterables.size(transactions), details.getData().getPeriodTransactions().size());
        }

        Assert.assertTrue(followItems.size() == 1);

        deleteUser(user);
    }

    @Test
    public void testCreateAndListFollowItems() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData(DemoCredentials.USER3.getUsername());

        List<Category> categories = new ArrayList<>(serviceFactory.getCategoryService().list(user, null));

        Collections.shuffle(categories);

        Category category1 = Iterables.find(categories, c -> (Objects.equal(c.getCode(), restaurantCategory)));

        // Create an expenses follow item.

        ExpensesFollowCriteria followCriteria1 = new ExpensesFollowCriteria();
        followCriteria1.setCategoryIds(Lists.newArrayList(category1.getId()));
        followCriteria1.setTargetAmount(-500d);

        FollowItem followItem1a = new FollowItem();
        followItem1a.setName("test");
        followItem1a.setType(FollowTypes.EXPENSES);

        followItem1a.setCriteria(SerializationUtils.serializeToString(followCriteria1));

        serviceFactory.getFollowService().create(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), followItem1a);

        List<FollowItem> followItems = serviceFactory.getFollowService().list(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), null);

        Assert.assertTrue(followItems.size() == 1);

        deleteUser(user);
    }

    @Test
    public void testCreateAndListOnParentCategory() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData(DemoCredentials.USER3.getUsername());

        List<Category> categories = new ArrayList<>(serviceFactory.getCategoryService().list(user, null));

        Collections.shuffle(categories);

        final Category categoryChild = Iterables
                .find(categories, c -> (Objects.equal(c.getCode(), restaurantCategory)));

        Category categoryParent = Iterables
                .find(categories, c -> (Objects.equal(c.getId(), categoryChild.getParent())));

        // Create an expenses follow item on parent category.

        ExpensesFollowCriteria followCriteria1 = new ExpensesFollowCriteria();
        followCriteria1.setCategoryIds(Lists.newArrayList(categoryParent.getId()));
        followCriteria1.setTargetAmount(-500d);

        FollowItem followItem1a = new FollowItem();
        followItem1a.setName("parent");
        followItem1a.setType(FollowTypes.EXPENSES);
        followItem1a.setCriteria(SerializationUtils.serializeToString(followCriteria1));

        FollowItem followItemParent = serviceFactory.getFollowService().create(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), followItem1a);

        // Create an expenses follow item for child category.

        ExpensesFollowCriteria followCriteria2 = new ExpensesFollowCriteria();
        followCriteria2.setCategoryIds(Lists.newArrayList(categoryChild.getId()));
        followCriteria2.setTargetAmount(-500d);

        FollowItem followItem2a = new FollowItem();
        followItem2a.setName("child");
        followItem2a.setType(FollowTypes.EXPENSES);
        followItem2a.setCriteria(SerializationUtils.serializeToString(followCriteria2));

        FollowItem followItemChild = serviceFactory.getFollowService().create(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), followItem2a);

        List<FollowItem> followItems = serviceFactory.getFollowService().list(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), null);

        Assert.assertTrue(followItems.size() == 2);

        for (FollowItem f : followItems) {
            Assert.assertTrue("Follow item " + f.getName(), f.getData() != null);
            Assert.assertTrue("Follow item " + f.getName(), f.getData().getHistoricalAmounts() != null);
            Assert.assertTrue("Follow item " + f.getName(), f.getData().getHistoricalAmounts().size() > 0);
        }

        // compare parent have larger values

        FollowItem parent = null;
        FollowItem child = null;
        for (FollowItem f : followItems) {
            if (f.getId().equals(followItemParent.getId())) {
                parent = f;
            }

            if (f.getId().equals(followItemChild.getId())) {
                child = f;
            }
        }

        for (StringDoublePair parentAmounts : parent.getData().getHistoricalAmounts()) {
            for (StringDoublePair childAmounts : child.getData().getHistoricalAmounts()) {
                if (parentAmounts.getKey().equals(childAmounts.getKey())) {
                    Assert.assertTrue(
                            "Amount should be bigger for parent, key:" + parentAmounts.getKey() + " parent amount:"
                                    + parentAmounts.getValue() + " child amount:" + childAmounts.getValue(),
                            Math.abs(parentAmounts.getValue()) >= Math.abs(childAmounts.getValue()));
                }
            }
        }

        deleteUser(user);
    }

    @Test
    public void testCreateExpensesFollowItem() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData(DemoCredentials.USER3.getUsername());

        List<Category> categories = new ArrayList<>(serviceFactory.getCategoryService().list(user, null));

        Collections.shuffle(categories);

        Category category1 = Iterables.find(categories, c -> (Objects.equal(c.getCode(), restaurantCategory)));

        // Create an expenses follow item.

        ExpensesFollowCriteria followCriteria1a = new ExpensesFollowCriteria();
        followCriteria1a.setCategoryIds(Lists.newArrayList(category1.getId()));
        followCriteria1a.setTargetAmount(-500d);

        FollowItem followItem1a = new FollowItem();
        followItem1a.setName("test");
        followItem1a.setType(FollowTypes.EXPENSES);
        followItem1a.setCriteria(SerializationUtils.serializeToString(followCriteria1a));

        FollowItem followItem1b = serviceFactory.getFollowService().create(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), followItem1a);
        ExpensesFollowCriteria followCriteria1b = SerializationUtils.deserializeFromString(followItem1b.getCriteria(),
                ExpensesFollowCriteria.class);

        Assert.assertEquals(followCriteria1a.getTargetAmount(), followCriteria1b.getTargetAmount());
        Assert.assertEquals(followItem1a.getType(), followItem1b.getType());

        // Cleanup.

        deleteUser(user);
    }

    @Test
    public void testSuggestSearchFollowItem() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData(DemoCredentials.USER3.getUsername());

        List<FollowItem> suggestedFollowItems = serviceFactory.getFollowService().suggestByType(new AuthenticatedUser(
                        HttpAuthenticationMethod.BASIC, user),
                FollowTypes.SEARCH);

        Assert.assertThat(suggestedFollowItems.size(), is(not(0)));

        // Cleanup.

        deleteUser(user);
    }

    @Test
    public void testCreateComplexFollowItems() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData(DemoCredentials.USER3.getUsername());

        List<Category> categories = new ArrayList<>(serviceFactory.getCategoryService().list(user, null));

        Collections.shuffle(categories);

        categories = Lists.newArrayList(Iterables.filter(categories,
                c -> (!Strings.isNullOrEmpty(c.getSecondaryName()) && c.getType() == CategoryTypes.EXPENSES)));

        Category category1 = Iterables.find(categories, c -> (Objects.equal(c.getCode(), restaurantCategory)));

        Category category2 = Iterables.find(categories, c -> (Objects.equal(c.getCode(), groceriesCategory)));

        ExpensesFollowCriteria followCriteria1a = new ExpensesFollowCriteria();
        followCriteria1a.setCategoryIds(Lists.newArrayList(category1.getId(), category2.getId()));
        followCriteria1a.setTargetAmount(-500d);

        FollowItem followItem1a = new FollowItem();
        followItem1a.setName("test");
        followItem1a.setType(FollowTypes.EXPENSES);
        followItem1a.setCriteria(SerializationUtils.serializeToString(followCriteria1a));

        FollowItem followItem1b = serviceFactory.getFollowService().create(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), followItem1a);
        ExpensesFollowCriteria followCriteria1b = SerializationUtils.deserializeFromString(followItem1a.getCriteria(),
                ExpensesFollowCriteria.class);

        Assert.assertEquals(followCriteria1a.getTargetAmount(), followCriteria1b.getTargetAmount());
        Assert.assertEquals(followItem1a.getType(), followItem1b.getType());

        // Cleanup.

        deleteUser(user);
    }

    @Test
    public void testDeleteFollowItem() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData();

        List<Category> categories = new ArrayList<>(serviceFactory.getCategoryService().list(user, null));

        Collections.shuffle(categories);

        Category category1 = Iterables.find(categories, c -> (!Strings.isNullOrEmpty(c.getSecondaryName())));

        ExpensesFollowCriteria followCriteria1 = new ExpensesFollowCriteria();
        followCriteria1.setCategoryIds(Lists.newArrayList(category1.getId()));
        followCriteria1.setTargetAmount(-500d);

        FollowItem followItem1a = new FollowItem();
        followItem1a.setName("test");
        followItem1a.setType(FollowTypes.EXPENSES);
        followItem1a.setCriteria(SerializationUtils.serializeToString(followCriteria1));

        FollowItem followItem1b = serviceFactory.getFollowService().create(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), followItem1a);

        serviceFactory.getFollowService().delete(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), followItem1b.getId());

        Assert.assertTrue(serviceFactory.getFollowService().list(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), null).size() == 0);

        // Cleanup.

        deleteUser(user);
    }

    @Test
    public void testUpdateFollowItem() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData();

        List<Category> categories = new ArrayList<>(serviceFactory.getCategoryService().list(user, null));

        Collections.shuffle(categories);

        Category category1 = Iterables.find(categories, c -> (!Strings.isNullOrEmpty(c.getSecondaryName())));

        ExpensesFollowCriteria followCriteria1a = new ExpensesFollowCriteria();
        followCriteria1a.setCategoryIds(Lists.newArrayList(category1.getId()));
        followCriteria1a.setTargetAmount(-500d);

        FollowItem followItem1a = new FollowItem();
        followItem1a.setName("test");
        followItem1a.setType(FollowTypes.EXPENSES);
        followItem1a.setCriteria(SerializationUtils.serializeToString(followCriteria1a));

        FollowItem followItem1b = serviceFactory.getFollowService().create(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), followItem1a);
        ExpensesFollowCriteria followCriteria1b = SerializationUtils.deserializeFromString(followItem1a.getCriteria(),
                ExpensesFollowCriteria.class);

        Assert.assertEquals(followCriteria1a.getTargetAmount(), followCriteria1b.getTargetAmount());
        Assert.assertEquals(followItem1a.getType(), followItem1b.getType());

        String preName = followItem1b.getName();

        followCriteria1b.setTargetAmount(-100d);
        followItem1b.setCriteria(SerializationUtils.serializeToString(followCriteria1b));
        followItem1b.setName("New name");

        FollowItem followItem1c = serviceFactory.getFollowService().update(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), followItem1b.getId(), followItem1b);
        ExpensesFollowCriteria followCriteria1c = SerializationUtils.deserializeFromString(followItem1c.getCriteria(),
                ExpensesFollowCriteria.class);

        Assert.assertEquals(Double.valueOf(-100d), followCriteria1c.getTargetAmount());
        Assert.assertEquals(preName, followItem1c.getName()); // Name has not changed. Is automatically localized for expenses (budgets).

        deleteUser(user);
    }

    @Test
    public void testFollowItemLocalized() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData();

        // Expected results
        String restaurantNameSwedish = "Restaurang";
        String restaurantNameEnglish = "Restaurants";

        UserProfile profile = new UserProfile();
        profile.setCurrency("SEK");
        profile.setPeriodAdjustedDay(25);
        profile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
        profile.setLocale("sv_SE");
        profile.setMarket("SE");

        user.setProfile(profile);

        // Get the Restaurant Category
        Category restaurantCategoryObj = injector.getInstance(CategoryRepository.class).findByCode(restaurantCategory, user.getLocale());

        ExpensesFollowCriteria followCriteria1a = new ExpensesFollowCriteria();
        followCriteria1a.setCategoryIds(Lists.newArrayList(restaurantCategoryObj.getId()));
        followCriteria1a.setTargetAmount(-500d);

        FollowItem followItem1a = new FollowItem();
        followItem1a.setName("test");
        followItem1a.setType(FollowTypes.EXPENSES);
        followItem1a.setCriteria(SerializationUtils.serializeToString(followCriteria1a));

        serviceFactory.getFollowService().create(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), followItem1a);

        // Get FollowItems (expected name in sv_SE)
        List<FollowItem> items = serviceFactory.getFollowService().list(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), "");
        Assert.assertEquals(1, items.size());
        Assert.assertEquals(restaurantNameSwedish, items.get(0).getName());

        // Change locale to en_US
        profile.setLocale("en_US");
        user.setProfile(profile);

        // Get followItems again (expect name in en_US)
        items = serviceFactory.getFollowService().list(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), "");
        Assert.assertEquals(1, items.size());
        Assert.assertEquals(restaurantNameEnglish, items.get(0).getName());

        // Cleanup.
        deleteUser(user);
    }
}
