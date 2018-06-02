package se.tink.backend.categorization.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.name.Named;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import se.tink.backend.categorization.ProbabilityCategorizer;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.client.FastTextServiceFactory;
import se.tink.backend.categorization.interfaces.Categorizer;
import se.tink.backend.categorization.interfaces.CategorizerFactory;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.categorization.learning.UserLearningCommand;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.categorization.rules.FastTextWebClassifier;
import se.tink.backend.categorization.rules.LabelIndexCache;
import se.tink.backend.categorization.rules.NaiveBayesCategorizationCommand;
import se.tink.backend.categorization.rules.NaiveBayesCategorizationCommandType;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.config.FastTextConfiguration;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Market;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.MetricRegistry;

public class FastTextCategorizerFactory implements CategorizerFactory, Closeable {

    private final ImmutableMap<Market.Code, FastTextWebClassifier> expenseFastTextClassifiersByMarket;
    private final ImmutableMap<Market.Code, FastTextWebClassifier> incomeFastTextClassifiersByMarket;
    private static final LogUtils log = new LogUtils(FastTextCategorizerFactory.class);
    private final CategoryConfiguration categoryConfiguration;
    private final CategorizationConfiguration categorizationConfiguration;
    private MetricRegistry metricRegistry;
    private ClusterCategories categories;
    private SimilarTransactionsSearcher similarTransactionsSearcher;
    private String categorizerName;

    @Inject
    public FastTextCategorizerFactory(CategoryConfiguration categoryConfiguration, MetricRegistry metricRegistry,
            ClusterCategories categories, SimilarTransactionsSearcher similarTransactionsSearcher,
            FastTextServiceFactory fastTextServiceFactory, @Named("fastTextExpenseConfiguration") List<FastTextConfiguration> expenseModels,
            @Named("fastTextIncomeConfiguration") List<FastTextConfiguration> incomeModels, @Named("fastTextCategorizerName") String categorizerName,
            CategorizationConfiguration categorizationConfiguration) {
        this.categoryConfiguration = categoryConfiguration;
        this.metricRegistry = metricRegistry;
        this.categories = categories;
        this.similarTransactionsSearcher = similarTransactionsSearcher;
        this.categorizationConfiguration = categorizationConfiguration;
        this.categorizerName = categorizerName;

        ImmutableMap.Builder<Market.Code, FastTextWebClassifier> expenseClassifiersBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<Market.Code, FastTextWebClassifier> incomeClassifiersBuilder = ImmutableMap.builder();
        expenseModels.forEach(m -> expenseClassifiersBuilder.put(m.getMarket(),
                new FastTextWebClassifier(fastTextServiceFactory.getFastTextClassifierService(), m.getName(),
                        m.getPreformatters().stream().map(p -> p.build()).collect(Collectors.toList()),
                        categorizationConfiguration, categoryConfiguration, categories)));
        incomeModels.forEach(m -> incomeClassifiersBuilder.put(m.getMarket(),
                new FastTextWebClassifier(fastTextServiceFactory.getFastTextClassifierService(), m.getName(),
                        m.getPreformatters().stream().map(p -> p.build()).collect(Collectors.toList()),
                        categorizationConfiguration, categoryConfiguration, categories)));
        expenseFastTextClassifiersByMarket = expenseClassifiersBuilder.build();
        incomeFastTextClassifiersByMarket = incomeClassifiersBuilder.build();
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
                buildClassifiers(provider, user, inStoreTransactions, labelIndexCache, citiesByMarket),
                categorizerName);
    }

    private Collection<Classifier> buildClassifiers(Provider provider, User user,
            Collection<Transaction> inStoreTransactions, LabelIndexCache labelIndexCache,
            CitiesByMarket citiesByMarket) {

        ImmutableList.Builder<Classifier> builder = ImmutableList.builder();

        Market.Code userMarket = user.getProfile().getMarketAsCode();
        boolean userIsInBetaMarket = !Objects.equals(userMarket, Market.Code.SE);
        FastTextWebClassifier incomeFastTextClassifier = incomeFastTextClassifiersByMarket.get(userMarket);

        // Use a Beta FastText income model if the user is in a Beta market, otherwise use NaiveBayes.
        if (userIsInBetaMarket && incomeFastTextClassifier != null) {
            builder.add(
                    // Use fasttext for incomes.
                    t -> t.getAmount() > 0 ? incomeFastTextClassifier.categorize(t) : Optional.empty()
            );
        } else {
            NaiveBayesCategorizationCommand
                    .build(labelIndexCache, citiesByMarket, NaiveBayesCategorizationCommandType.INCOME, provider)
                    .ifPresent(builder::add);
        }

        // Try to get the model suitable for the user's market. If it doesn't exist, choose the default market model.
        FastTextWebClassifier expenseFastTextClassifierForMarket = expenseFastTextClassifiersByMarket.get(userMarket);
        FastTextWebClassifier expenseFastTextClassifier = expenseFastTextClassifierForMarket != null ?
                expenseFastTextClassifierForMarket :
                expenseFastTextClassifiersByMarket.get(categorizationConfiguration.getDefaultMarket());

        if (expenseFastTextClassifier != null) {
            builder.add(
                    // Use fasttext for expenses.
                    t -> t.getAmount() > 0 ? Optional.empty() : expenseFastTextClassifier.categorize(t)
            );
        } else {
            log.error("Could not find model for default market. This should not happen and is a misconfiguration.");
        }

        builder.add(
                new UserLearningCommand(
                        user.getId(), similarTransactionsSearcher, categories,
                        inStoreTransactions
                )
        );

        return builder.build();
    }

    @PreDestroy
    @Override
    public void close() throws IOException {
    }
}
