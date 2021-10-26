package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step;

import se.tink.libraries.enums.MarketCode;

public interface MarketCodeStep {

    ProviderNameStep withMarketCode(MarketCode marketCode);
}
