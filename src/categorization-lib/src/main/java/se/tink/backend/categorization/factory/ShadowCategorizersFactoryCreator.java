package se.tink.backend.categorization.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.client.FastTextServiceFactory;
import se.tink.backend.categorization.interfaces.CategorizerFactory;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.config.FastTextConfiguration;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.core.ClusterCategories;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.MetricRegistry;

// TODO: Migrate this to Guice module.
@Singleton
public class ShadowCategorizersFactoryCreator implements Closeable {
    private static final LogUtils log = new LogUtils(ShadowCategorizersFactoryCreator.class);

    private final ImmutableList<CategorizerFactory> classifiers;

    @Inject
    public ShadowCategorizersFactoryCreator(Cluster cluster, CategorizationConfiguration categorizationConfiguration,
            FastTextServiceFactory fastTextServiceFactory, CategoryConfiguration categoryConfiguration,
            MetricRegistry metricRegistry, ClusterCategories categories, ElasticSearchClient elasticSearchClient) {

        Map<String, List<FastTextConfiguration>> expenseModelsByName = categorizationConfiguration
                .getShadowCategorizers().stream().collect(Collectors.groupingBy(FastTextConfiguration::getName));

        Iterator<? extends CategorizerFactory> shadowCategorizersIterator = expenseModelsByName.keySet()
                .stream()
                .map(name -> {
                    log.info("Creating shadow categorizer for model: " + name);

                    // There can be multiple models of the same type, but for different markets.
                    List<FastTextConfiguration> expenseModels = expenseModelsByName.get(name);

                    if (categorizationConfiguration.isMicroserviceEnabled()) {
                        return new FastTextCategorizerFactory(
                                categoryConfiguration,
                                metricRegistry,
                                categories,
                                elasticSearchClient.getSimilarTransactionsSearcher(),
                                fastTextServiceFactory,
                                expenseModels,
                                Lists.newArrayList(),
                                name,
                                categorizationConfiguration);
                    } else {
                        return new FastTextInProcessCategorizerFactory(cluster,
                                categoryConfiguration,
                                categorizationConfiguration,
                                metricRegistry,
                                categories,
                                elasticSearchClient.getSimilarTransactionsSearcher(),
                                new File(categorizationConfiguration.getExecutable()),
                                expenseModels,
                                Lists.newArrayList(),
                                name);
                    }
                })
                .iterator();
        classifiers = ImmutableList.copyOf(shadowCategorizersIterator);
    }

    @PreDestroy
    public void close() throws IOException {
        for(CategorizerFactory c : classifiers) {
            if(c instanceof Closeable) {
                ((Closeable) c).close();
            }
        }
    }

    public Collection<CategorizerFactory> build() {
        return classifiers;
    }
}
