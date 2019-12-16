package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import java.math.BigDecimal;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

public class ExactCurrencyAmountMapper
        implements Mapper<
                ExactCurrencyAmount, se.tink.sa.services.fetch.account.ExactCurrencyAmount> {

    @Override
    public ExactCurrencyAmount map(
            se.tink.sa.services.fetch.account.ExactCurrencyAmount source, MappingContext context) {
        long unscaledValue = source.getValue().getUnscaledValue();
        int scale = source.getValue().getScale();
        return ExactCurrencyAmount.of(
                BigDecimal.valueOf(unscaledValue, scale), source.getCurrencyCode());
    }
}
