package se.tink.backend.system.workers.processor.formatting;

import se.tink.backend.core.Market;

public class BasicDescriptionExtractorFactory implements MarketDescriptionExtractorFactory {
    private final MarketDescriptionFormatterFactory descriptionFactory;

    public BasicDescriptionExtractorFactory(MarketDescriptionFormatterFactory marketDescriptionFormatterFactory) {
        descriptionFactory = marketDescriptionFormatterFactory;
    }

    @Override
    public DescriptionExtractor get(Market.Code market) {
        return new BasicDescriptionExtractor(descriptionFactory.get(market));
    }
}
