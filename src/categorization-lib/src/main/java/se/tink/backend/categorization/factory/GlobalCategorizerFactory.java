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
import se.tink.backend.categorization.rules.MerchantMappingCommand;
import se.tink.backend.categorization.rules.LabelIndexCache;
import se.tink.backend.categorization.rules.NaiveBayesCategorizationCommand;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.MetricRegistry;

public class GlobalCategorizerFactory implements CategorizerFactory {
    private CategoryConfiguration categoryConfiguration;
    private MetricRegistry metricRegistry;
    private ClusterCategories categories;
    private SimilarTransactionsSearcher similarTransactionsSearcher;

    @Inject
    public GlobalCategorizerFactory(CategoryConfiguration categoryConfiguration,
            MetricRegistry metricRegistry, ClusterCategories categories,
            SimilarTransactionsSearcher similarTransactionsSearcher) {

        this.categoryConfiguration = categoryConfiguration;
        this.metricRegistry = metricRegistry;
        this.categories = categories;
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
                buildClassifiers(provider, user, inStoreTransactions, labelIndexCache, citiesByMarket,
                        categorizationConfiguration),
                "oxford");
    }

    private Collection<Classifier> buildClassifiers(Provider provider, User user,
            Collection<Transaction> inStoreTransactions, LabelIndexCache labelIndexCache,
            CitiesByMarket citiesByMarket, CategorizationConfiguration categorizationConfiguration) {
        ImmutableList.Builder<Classifier> builder = ImmutableList.builder();

        NaiveBayesCategorizationCommand.buildAllTypes(labelIndexCache, citiesByMarket, provider).forEach(builder::add);

        MerchantMappingCommand.build(categorizationConfiguration.getMerchantsFile()).ifPresent(builder::add);

        builder.add(new UserLearningCommand(
                user.getId(), similarTransactionsSearcher, categories,
                inStoreTransactions
        ));

        return builder.build();
    }
}
