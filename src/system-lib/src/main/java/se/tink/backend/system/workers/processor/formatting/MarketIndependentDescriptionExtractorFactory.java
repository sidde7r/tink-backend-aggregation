package se.tink.backend.system.workers.processor.formatting;

import com.google.common.base.MoreObjects;
import se.tink.backend.core.Market;

public class MarketIndependentDescriptionExtractorFactory implements MarketDescriptionExtractorFactory {
    private final DescriptionExtractor delegate;

    public MarketIndependentDescriptionExtractorFactory(DescriptionExtractor delegate) {
        this.delegate = delegate;
    }

    @Override
    public DescriptionExtractor get(Market.Code market) {
        return delegate;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("delegate", delegate)
                .toString();
    }
}
