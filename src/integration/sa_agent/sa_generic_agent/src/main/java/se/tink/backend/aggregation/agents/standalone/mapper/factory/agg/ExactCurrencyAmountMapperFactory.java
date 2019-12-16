package se.tink.backend.aggregation.agents.standalone.mapper.factory.agg;

import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg.ExactCurrencyAmountMapper;

public class ExactCurrencyAmountMapperFactory {

    private ExactCurrencyAmountMapperFactory() {}

    public static ExactCurrencyAmountMapperFactory newInstance() {
        return new ExactCurrencyAmountMapperFactory();
    }

    public ExactCurrencyAmountMapper exactCurrencyAmountMapper() {
        return new ExactCurrencyAmountMapper();
    }
}
