package se.tink.backend.categorization.factory;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.Collection;
import se.tink.backend.categorization.ProbabilityCategorizer;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.interfaces.Categorizer;
import se.tink.backend.categorization.interfaces.CategorizerFactory;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.categorization.learning.UserLearningCommand;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.categorization.rules.AbnAmroCategorizationCommand;
import se.tink.backend.categorization.rules.AbnAmroIcsCategorizationCommand;
import se.tink.backend.categorization.rules.LabelIndexCache;
import se.tink.backend.categorization.rules.NaiveBayesCategorizationCommand;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.libraries.metrics.MetricRegistry;

public class LeedsCategorizerFactory implements CategorizerFactory {
    private ClusterCategories categories;
    private CategoryConfiguration categoryConfiguration;
    private MetricRegistry metricRegistry;
    private SimilarTransactionsSearcher similarTransactionsSearcher;

    @Inject
    public LeedsCategorizerFactory(
            ClusterCategories categories,
            CategoryConfiguration categoryConfiguration, MetricRegistry metricRegistry,
            SimilarTransactionsSearcher similarTransactionsSearcher) {

        this.categories = categories;
        this.categoryConfiguration = categoryConfiguration;
        this.metricRegistry = metricRegistry;
        this.similarTransactionsSearcher = similarTransactionsSearcher;
    }

    @Override
    public Categorizer build(User user, Provider provider, Collection<Transaction> inStoreTransactions,
            LabelIndexCache labelIndexCache, CitiesByMarket citiesByMarket,
            CategorizationConfiguration categorizationConfiguration) {
        return new ProbabilityCategorizer(
                user,
                categoryConfiguration,
                metricRegistry,
                categories,
                buildCategorizers(provider, user, inStoreTransactions, labelIndexCache, citiesByMarket),
                "leeds");
    }

    private Collection<Classifier> buildCategorizers(Provider provider, User user,
            Collection<Transaction> inStoreTransactions, LabelIndexCache labelIndexCache,
            CitiesByMarket citiesByMarket) {

        ImmutableList.Builder<Classifier> builder = ImmutableList.builder();

        NaiveBayesCategorizationCommand.buildAllTypes(labelIndexCache, citiesByMarket, provider).forEach(builder::add);
        AbnAmroCategorizationCommand.build(provider).ifPresent(builder::add);
        AbnAmroIcsCategorizationCommand.build(provider).ifPresent(builder::add);

        builder.add(new UserLearningCommand(
                user.getId(), similarTransactionsSearcher, categories,
                inStoreTransactions
        ));

        return builder.build();
    }
}
