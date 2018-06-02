package se.tink.backend.categorization.factory;

import com.google.inject.Inject;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.interfaces.CategorizerFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.core.ClusterCategories;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.MetricRegistry;

public class DefaultCategorizerFactoryCreator {
    private final CategorizerFactory categorizerFactory;

    // TODO: Migrate to cluster specific Guice module.
    @Inject
    private DefaultCategorizerFactoryCreator(
            Cluster cluster,
            CornwallCategorizerFactory cornwallCategorizerFactory,
            LeedsCategorizerFactory leedsCategorizerFactory,
            GlobalCategorizerFactory globalCategorizerFactory) {

        switch (cluster) {
        case CORNWALL:
            categorizerFactory = cornwallCategorizerFactory;
            break;
        case ABNAMRO:
            categorizerFactory = leedsCategorizerFactory;
            break;
        default:
            categorizerFactory = globalCategorizerFactory;
            break;
        }
    }

    @Deprecated
    public static DefaultCategorizerFactoryCreator fromServiceContext(ServiceContext context,
            MetricRegistry metricRegistry, ClusterCategories categories, ElasticSearchClient elasticSearchClient) {
        CategoryConfiguration categoryConfiguration = context.getCategoryConfiguration();

        SimilarTransactionsSearcher similarTransactionsSearcher = elasticSearchClient.getSimilarTransactionsSearcher();

        return new DefaultCategorizerFactoryCreator(
                context.getConfiguration().getCluster(),
                new CornwallCategorizerFactory(
                        categoryConfiguration,
                        metricRegistry,
                        categories,
                        similarTransactionsSearcher
                ),
                new LeedsCategorizerFactory(
                        categories,
                        categoryConfiguration,
                        metricRegistry,
                        similarTransactionsSearcher
                ),
                new GlobalCategorizerFactory(
                        categoryConfiguration,
                        metricRegistry,
                        categories,
                        similarTransactionsSearcher
                )
        );
    }

    public CategorizerFactory build() {
        return categorizerFactory;
    }
}
