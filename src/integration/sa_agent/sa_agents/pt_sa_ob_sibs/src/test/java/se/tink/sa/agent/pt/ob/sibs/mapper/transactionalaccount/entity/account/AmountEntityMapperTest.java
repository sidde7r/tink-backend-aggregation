package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.entity.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import se.tink.sa.agent.pt.ob.sibs.mapper.common.BigDecimalMapper;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AmountEntity;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.common.BigDecimal;
import se.tink.sa.services.fetch.account.ExactCurrencyAmount;

public class AmountEntityMapperTest {

    private static final String AMOUNT = "1234.56789";

    @InjectMocks private AmountEntityMapper amountEntityMapper;

    @Mock private BigDecimalMapper bigDecimalMapper;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMapping() {
        AmountEntity amount = new AmountEntity();

        Mockito.when(bigDecimalMapper.map(Mockito.eq(AMOUNT), Mockito.any(MappingContext.class)))
                .thenReturn(BigDecimal.newBuilder().build());

        amount.setContent(AMOUNT);
        amount.setCurrency("EUR");
        ExactCurrencyAmount exactCurrencyAmount = amountEntityMapper.map(amount);

        assertNotNull(exactCurrencyAmount);
        assertEquals("EUR", exactCurrencyAmount.getCurrencyCode());
        assertNotNull(exactCurrencyAmount.getValue());

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MappingContext> argumentCaptorMappingContext =
                ArgumentCaptor.forClass(MappingContext.class);
        Mockito.verify(bigDecimalMapper, Mockito.times(1))
                .map(argumentCaptor.capture(), argumentCaptorMappingContext.capture());

        assertEquals(AMOUNT, argumentCaptor.getValue());

        assertNotNull(argumentCaptorMappingContext.getValue());
    }
}
