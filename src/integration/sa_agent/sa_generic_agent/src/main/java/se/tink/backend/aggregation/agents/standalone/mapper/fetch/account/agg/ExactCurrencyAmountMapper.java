package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

public class ExactCurrencyAmountMapper
        implements Mapper<
                ExactCurrencyAmount, se.tink.sa.services.fetch.account.ExactCurrencyAmount> {

    @Override
    public ExactCurrencyAmount map(
            se.tink.sa.services.fetch.account.ExactCurrencyAmount source, MappingContext context) {
        // MARKER
        //        long value = source.getUnscaledValue();
        //        int scale = source.getScale();

        //        return ExactCurrencyAmount.of(new BigDe, source.getCurrencyCode());
        return null;
    }
}
