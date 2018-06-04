package se.tink.backend.common.mapper;

import org.junit.Test;
import se.tink.backend.core.product.ProductType;
import static org.junit.Assert.assertEquals;

public class CoreProductTypeMapperTest {
    @Test
    public void mapper() {
        assertEquals("SAVINGS_ACCOUNT", CoreProductTypeMapper.toAggregation(ProductType.SAVINGS_ACCOUNT).name());
        assertEquals("MORTGAGE", CoreProductTypeMapper.toAggregation(ProductType.MORTGAGE).name());
        assertEquals("RESIDENCE_VALUATION", CoreProductTypeMapper.toAggregation(ProductType.RESIDENCE_VALUATION).name());
    }
}
