package se.tink.backend.system.workers.processor.formatting;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.backend.core.Market;

public class MarketSpecificDescriptionFormatterFactory implements MarketDescriptionFormatterFactory {
    private final DescriptionFormatter fallback;
    private final ImmutableMap<Market.Code, DescriptionFormatter> delegates;

    public MarketSpecificDescriptionFormatterFactory(
            Map<Market.Code, DescriptionFormatter> delegates,
            DescriptionFormatter fallback
    ) {
        this.delegates = ImmutableMap.copyOf(delegates);
        this.fallback = Preconditions.checkNotNull(fallback);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fallback", fallback)
                .add("delegates", delegates)
                .toString();
    }

    @Override
    public DescriptionFormatter get(Market.Code market) {
        return delegates.getOrDefault(market, fallback);
    }
}
