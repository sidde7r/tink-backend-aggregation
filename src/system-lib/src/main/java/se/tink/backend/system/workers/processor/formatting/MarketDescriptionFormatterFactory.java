package se.tink.backend.system.workers.processor.formatting;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import se.tink.backend.core.Market;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.utils.CityDescriptionTrimmer;
import se.tink.backend.utils.StringExtrapolator;

public interface MarketDescriptionFormatterFactory {
    DescriptionFormatter get(Market.Code market);

    // TODO: Migrate to cluster specific Guice module.
    static MarketDescriptionFormatterFactory byCluster(Cluster cluster) {
        switch (cluster) {
        default:
            // Not having this as a constant in interface since interface constants can't be private.
            final String CORPUS_FILENAME = "data/seeding/extrapolation-corpus-greedy.txt";

            StringExtrapolator extrapolator;
            try {
                extrapolator = StringExtrapolator.load(
                        CORPUS_FILENAME,
                        input -> input.toLowerCase().replaceAll("[ \t]+", " ").trim()
                );
            } catch (IOException e) {
                throw new RuntimeException("Could not load the corpus.", e);
            }

            return new MarketSpecificDescriptionFormatterFactory(
                    ImmutableMap.of(
                            Market.Code.NL, new NlDescriptionFormatter(),
                            Market.Code.US, new UsDescriptionFormatter()
                    ),
                    new SeDescriptionFormatter(extrapolator)
            );
        case CORNWALL:
            return new MarketIndependentDescriptionFormatterFactory(new BasicDescriptionFormatter());
        case ABNAMRO:
            return new MarketIndependentDescriptionFormatterFactory(
                    new NlDescriptionFormatter(new FuzzyCityTrimmer(CityDescriptionTrimmer.builder().build()))
            );
        }
    }
}
