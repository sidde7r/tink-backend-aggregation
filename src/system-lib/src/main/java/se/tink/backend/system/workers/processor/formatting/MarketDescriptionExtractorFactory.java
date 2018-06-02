package se.tink.backend.system.workers.processor.formatting;

import se.tink.backend.core.Market;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.utils.CityDescriptionTrimmer;
import se.tink.backend.utils.StringExtrapolator;

public interface MarketDescriptionExtractorFactory {
    DescriptionExtractor get(Market.Code market);

    // TODO: Migrate to cluster specific Guice module.
    static MarketDescriptionExtractorFactory byCluster(Cluster cluster) {
        switch (cluster) {
        case ABNAMRO:
            return new MarketIndependentDescriptionExtractorFactory(
                    new NlDescriptionFormatter(new FuzzyCityTrimmer(CityDescriptionTrimmer.builder().build()))
            );
        case TINK:
            return new MarketIndependentDescriptionExtractorFactory(
                    // Since this is used as an extractor we don't need the extrapolator.
                    new SeDescriptionFormatter(new StringExtrapolator())
            );
        default:
            return new BasicDescriptionExtractorFactory(MarketDescriptionFormatterFactory.byCluster(cluster));
        }
    }
}
