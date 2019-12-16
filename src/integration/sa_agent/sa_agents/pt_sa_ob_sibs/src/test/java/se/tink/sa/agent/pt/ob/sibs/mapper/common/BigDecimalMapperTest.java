package se.tink.sa.agent.pt.ob.sibs.mapper.common;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import se.tink.sa.services.common.BigDecimal;

public class BigDecimalMapperTest {

    private static final String AMOUNT = "1234.56789";

    private BigDecimalMapper bigDecimalMapper;

    @Before
    public void init() {
        bigDecimalMapper = new BigDecimalMapper();
    }

    @Test
    public void testBigDecimalMapper() {

        BigDecimal value = bigDecimalMapper.map(AMOUNT);
        TestCase.assertNotNull(value);

        TestCase.assertEquals(123456789L, value.getUnscaledValue());
        TestCase.assertEquals(5, value.getScale());
    }
}
