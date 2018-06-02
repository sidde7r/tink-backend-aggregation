package se.tink.backend.categorization.rules;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.categorization.CategorizationVector;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.main.TestUtils;

public class NaiveBayesCategorizationCommandTest {
    private User user;
    private Provider provider;
    private NaiveBayesCategorizationCommand incomeCategorization;
    private CitiesByMarket citiesByMarket;

    private NaiveBayesCategorizationCommand buildExpenseCategorizationCommand(Cluster cluster) throws Exception {
        return new NaiveBayesCategorizationCommand(
                LabelIndexCache.build(cluster), citiesByMarket, NaiveBayesCategorizationCommandType.EXPENSE,
                provider);
    }

    @Test
    public void testExpenseDescription() throws Exception {
        Transaction transaction = TestUtils
                .createTransaction("HEMKoP Taby C", -54, new Date(), user.getId(), null, null);

        NaiveBayesCategorizationCommand expenseCategorization = buildExpenseCategorizationCommand(
                Cluster.TINK);
        CategorizationVector categorizationVector = expenseCategorization.categorize(transaction).get().vector;

        System.out.println(categorizationVector.getDistribution());

        Assert.assertTrue("Categorization vector is empty", categorizationVector.getDistribution().size() > 0);

        Map<String, Double> mostProbable = categorizationVector.getMostProbable(0d);

        Assert.assertTrue("Determined incorrect category",
                mostProbable.keySet().contains(SECategories.Codes.EXPENSES_FOOD_GROCERIES));

    }

    @Test
    public void testExpenseDescriptionForSEBCluster() throws Exception {
        Transaction transaction = TestUtils
                .createTransaction("HEMKoP Taby C", -54, new Date(), user.getId(), null, null);

        NaiveBayesCategorizationCommand expenseCategorization = buildExpenseCategorizationCommand(
                Cluster.CORNWALL);
        CategorizationVector categorizationVector = expenseCategorization.categorize(transaction).get().vector;

        System.out.println(categorizationVector.getDistribution());

        Assert.assertTrue("Categorization vector is empty", categorizationVector.getDistribution().size() > 0);

        Map<String, Double> mostProbable = categorizationVector.getMostProbable(0d);

        Assert.assertTrue("Determined incorrect category",
                mostProbable.keySet().contains("expenses:provisions.groceries"));

    }

    @Before
    public void commonSetUp() throws Exception {
        user = TestUtils.createUser("test");
        user.setFlags(Lists.newArrayList(FeatureFlags.TINK_EMPLOYEE));
        provider = new Provider();
        provider.setMarket(user.getProfile().getMarket());
        citiesByMarket = CitiesByMarket.build(ImmutableList.of(provider));

        incomeCategorization = new NaiveBayesCategorizationCommand(
                LabelIndexCache.build(Cluster.TINK), citiesByMarket,
                NaiveBayesCategorizationCommandType.INCOME,
                provider);
    }

    @Test
    public void testIncomeDescription() throws Exception {
        // `Swish` description doesn't work :(
        Transaction transaction = TestUtils
                .createTransaction("Studstöd", 10000, new Date(), user.getId(), null, null);

        CategorizationVector categorizationVector = incomeCategorization.categorize(transaction).get().vector;

        System.out.println(categorizationVector.getDistribution());

        Assert.assertTrue("Categorization vector is empty", categorizationVector.getDistribution().size() > 0);

        Map<String, Double> mostProbable = categorizationVector.getMostProbable(0d);

        Assert.assertTrue("Determined incorrect category",
                mostProbable.keySet().contains(SECategories.Codes.INCOME_BENEFITS_OTHER));

    }
}
