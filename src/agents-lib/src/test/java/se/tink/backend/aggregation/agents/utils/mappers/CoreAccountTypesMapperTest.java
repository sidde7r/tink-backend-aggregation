package se.tink.backend.aggregation.agents.utils.mappers;

import org.junit.Test;
import se.tink.libraries.account.enums.AccountTypes;
import static org.junit.Assert.assertEquals;

public class CoreAccountTypesMapperTest {
    @Test
    public void exhaustiveCorrectMapping_fromMainToAggregationTypes() {
        // CHECKING, SAVINGS, INVESTMENT, MORTGAGE, CREDIT_CARD, LOAN, DUMMY, PENSION, OTHER, EXTERNAL;
        assertEquals("CHECKING", CoreAccountTypesMapper.toAggregation(AccountTypes.CHECKING).name());
        assertEquals("SAVINGS", CoreAccountTypesMapper.toAggregation(AccountTypes.SAVINGS).name());
        assertEquals("INVESTMENT", CoreAccountTypesMapper.toAggregation(AccountTypes.INVESTMENT).name());
        assertEquals("MORTGAGE", CoreAccountTypesMapper.toAggregation(AccountTypes.MORTGAGE).name());
        assertEquals("CREDIT_CARD", CoreAccountTypesMapper.toAggregation(AccountTypes.CREDIT_CARD).name());
        assertEquals("LOAN", CoreAccountTypesMapper.toAggregation(AccountTypes.LOAN).name());
        assertEquals("DUMMY", CoreAccountTypesMapper.toAggregation(AccountTypes.DUMMY).name());
        assertEquals("PENSION", CoreAccountTypesMapper.toAggregation(AccountTypes.PENSION).name());
        assertEquals("OTHER", CoreAccountTypesMapper.toAggregation(AccountTypes.OTHER).name());
        assertEquals("EXTERNAL", CoreAccountTypesMapper.toAggregation(AccountTypes.EXTERNAL).name());
    }
}
