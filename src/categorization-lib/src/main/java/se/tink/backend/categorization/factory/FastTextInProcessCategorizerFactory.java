package se.tink.backend.categorization.factory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.PreDestroy;
import se.tink.backend.categorization.ProbabilityCategorizer;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.interfaces.Categorizer;
import se.tink.backend.categorization.interfaces.CategorizerFactory;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.categorization.learning.UserLearningCommand;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.categorization.rules.AbnAmroCategorizationCommand;
import se.tink.backend.categorization.rules.AbnAmroIcsCategorizationCommand;
import se.tink.backend.categorization.rules.FastTextClassifier;
import se.tink.backend.categorization.rules.MerchantMappingCommand;
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
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.log.LogUtils;
import se.tink.libraries.metrics.MetricRegistry;

public class FastTextInProcessCategorizerFactory implements CategorizerFactory, Closeable {
    private static final LogUtils log = new LogUtils(FastTextInProcessCategorizerFactory.class);

    private Cluster cluster;
    private final FastTextProcessorCategorizerFactory expenseCategorizerFactory;
    private final FastTextProcessorCategorizerFactory incomeCategorizerFactory;
    private final Map<Market.Code, FastTextClassifier> expenseFastTextClassifiersByMarket;
    private final Map<Market.Code, FastTextClassifier> incomeFastTextClassifiersByMarket;

    private final CategorizationConfiguration categorizationConfiguration;
    private final CategoryConfiguration categoryConfiguration;
    private MetricRegistry metricRegistry;
    private ClusterCategories categories;
    private SimilarTransactionsSearcher similarTransactionsSearcher;
    private String categorizerName;

    // TODO: Make private (https://github.com/google/guice/wiki/KeepConstructorsHidden).
    @Inject
    public FastTextInProcessCategorizerFactory(Cluster cluster, CategoryConfiguration categoryConfiguration,
            CategorizationConfiguration categorizationConfiguration, MetricRegistry metricRegistry,
            ClusterCategories categories, SimilarTransactionsSearcher similarTransactionsSearcher,
            @Named("fastTextExecutable") File fasttextExecutable,
            @Named("fastTextExpenseConfiguration") List<FastTextConfiguration> expenseModels,
            @Named("fastTextIncomeConfiguration") List<FastTextConfiguration> incomeModels, String categorizerName) {

        this.cluster = cluster;
        this.categorizationConfiguration = categorizationConfiguration;
        this.categoryConfiguration = categoryConfiguration;
        this.metricRegistry = metricRegistry;
        this.categories = categories;
        this.similarTransactionsSearcher = similarTransactionsSearcher;
        this.categorizerName = categorizerName;

        Preconditions.checkArgument(fasttextExecutable.exists());

        expenseCategorizerFactory = new FastTextProcessorCategorizerFactory(3, fasttextExecutable, expenseModels,
                categoryConfiguration, categorizationConfiguration, categories);
        incomeCategorizerFactory = new FastTextProcessorCategorizerFactory(3, fasttextExecutable, incomeModels,
                categoryConfiguration, categorizationConfiguration, categories);
        this.expenseFastTextClassifiersByMarket = expenseCategorizerFactory.build();
        this.incomeFastTextClassifiersByMarket = incomeCategorizerFactory.build();

        dryRunModels();
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
        boolean userIsInBetaMarket = Objects.equals(userMarket, Market.Code.NO);
        FastTextClassifier incomeFastTextClassifier = incomeFastTextClassifiersByMarket.get(userMarket);

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
        Classifier expenseFastTextClassifierForMarket = expenseFastTextClassifiersByMarket.get(userMarket);
        Classifier expenseFastTextClassifier = expenseFastTextClassifierForMarket != null ?
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

        // TODO: replace me
        if (cluster.equals(Cluster.ABNAMRO)) {
            AbnAmroCategorizationCommand.build(provider).ifPresent(builder::add);
            AbnAmroIcsCategorizationCommand.build(provider).ifPresent(builder::add);
        }
        MerchantMappingCommand.build(categorizationConfiguration.getMerchantsFile()).ifPresent(builder::add);

        builder.add(
                new UserLearningCommand(
                        user.getId(), similarTransactionsSearcher, categories,
                        inStoreTransactions
                )
        );

        return builder.build();
    }

    private void dryRunModels() {
        // Dry-run expense models.
        for (FastTextClassifier classifier : expenseFastTextClassifiersByMarket.values()) {
            classifier.predict("test");
        }

        // Dry-run income models.
        for (FastTextClassifier classifier : incomeFastTextClassifiersByMarket.values()) {
            classifier.predict("test");
        }
    }

    @PreDestroy
    @Override
    public void close() throws IOException {
        expenseCategorizerFactory.stop();
        incomeCategorizerFactory.stop();
    }
}
