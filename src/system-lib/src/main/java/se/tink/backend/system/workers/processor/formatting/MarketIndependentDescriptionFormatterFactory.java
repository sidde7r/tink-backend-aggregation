package se.tink.backend.system.workers.processor.formatting;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import se.tink.backend.core.Market;

public class MarketIndependentDescriptionFormatterFactory implements MarketDescriptionFormatterFactory {
    private DescriptionFormatter delegate;

    public MarketIndependentDescriptionFormatterFactory(DescriptionFormatter delegate) {
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    @Override
    public DescriptionFormatter get(Market.Code market) {
        return delegate;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("delegate", delegate)
                .toString();
    }
}
