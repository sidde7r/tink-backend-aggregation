package se.tink.backend.system.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import java.io.File;
import java.util.List;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.factory.CornwallCategorizerFactory;
import se.tink.backend.categorization.factory.FastTextCategorizerFactory;
import se.tink.backend.categorization.factory.FastTextInProcessCategorizerFactory;
import se.tink.backend.categorization.factory.GlobalCategorizerFactory;
import se.tink.backend.categorization.factory.LeedsCategorizerFactory;
import se.tink.backend.categorization.interfaces.CategorizerFactory;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.config.FastTextConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.system.guice.providers.ChainFactoryProvider;
import se.tink.backend.system.workers.processor.chaining.ChainFactory;
import se.tink.backend.system.workers.processor.chaining.DefaultUserChainFactoryCreator;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.MetricRegistry;

public class SystemProcessingModule extends AbstractModule {

    private final ServiceConfiguration configuration;

    public SystemProcessingModule(ServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(ChainFactory.class).toProvider(ChainFactoryProvider.class).in(Scopes.SINGLETON);
        bind(DefaultUserChainFactoryCreator.class).in(Scopes.SINGLETON);

        if (configuration.getCategorization().isMicroserviceEnabled()) {
            bind(CategorizerFactory.class).annotatedWith(Names.named("fastTextCategorizerFactory"))
                    .to(FastTextCategorizerFactory.class);
        } else if(configuration.getCategorization().isBetaForAllUsers() || configuration.getCategorization().isBetaForFeatureFlaggedUsers()){
            bind(CategorizerFactory.class).annotatedWith(Names.named("fastTextCategorizerFactory"))
                    .to(FastTextInProcessCategorizerFactory.class);
        } else {
            bind(CategorizerFactory.class).annotatedWith(Names.named("fastTextCategorizerFactory"))
                    .toProvider(Providers.of(null));
        }
    }

    @Provides
    @Named("legacyCategorizerFactory")
    CategorizerFactory providesLegacyCategorizerFactory(Cluster cluster, CategoryConfiguration categoryConfiguration,
            MetricRegistry metricRegistry, ClusterCategories categories,
            SimilarTransactionsSearcher similarTransactionsSearcher) {
        switch (cluster) {
        case SEB:
            return new CornwallCategorizerFactory(categoryConfiguration,
                    metricRegistry, categories, similarTransactionsSearcher);
        case ABNAMRO:
            return new LeedsCategorizerFactory(categories, categoryConfiguration,
                    metricRegistry, similarTransactionsSearcher);
        default:
            return new GlobalCategorizerFactory(categoryConfiguration,
                    metricRegistry, categories, similarTransactionsSearcher);
        }
    }

    @Provides
    @Named("fastTextExecutable")
    File provideFastTextExecutable(CategorizationConfiguration categorizationConfiguration) {
        return new File(categorizationConfiguration.getExecutable());
    }

    @Provides
    @Named("fastTextExpenseConfiguration")
    List<FastTextConfiguration> provideFastTextExpenseConfiguration(
            CategorizationConfiguration categorizationConfiguration) {
        return categorizationConfiguration.getFastTextCategorizers();
    }

    @Provides
    @Named("fastTextIncomeConfiguration")
    List<FastTextConfiguration> provideFastTextIncomeConfiguration(
            CategorizationConfiguration categorizationConfiguration) {
        return categorizationConfiguration.getFastTextIncomeCategorizers();
    }

    @Provides
    @Named("fastTextCategorizerName")
    String provideFastTextCategorizerName(Cluster cluster) {
        return String.format("%s-%s", cluster.name(), "fasttext");
    }
}
