package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.entity.account;

import static junit.framework.TestCase.*;

import org.junit.Before;
import org.junit.Test;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AmountEntity;
import se.tink.sa.services.fetch.account.ExactCurrencyAmount;
import src.integration.sa_agent.sa_agents.pt_sa_ob_sibs.src.main.java.se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.entity.account.AmountEntityMapper;

public class AmountEntityMapperTest {

    private AmountEntityMapper amountEntityMapper;

    @Before
    public void init() {
        amountEntityMapper = new AmountEntityMapper();
    }

    @Test
    public void testMapping() {
        AmountEntity amount = new AmountEntity();
        amount.setContent("1234.56789");
        amount.setCurrency("EUR");
        ExactCurrencyAmount exactCurrencyAmount = amountEntityMapper.map(amount);

        assertNotNull(exactCurrencyAmount);
        assertEquals("EUR", exactCurrencyAmount.getCurrencyCode());
        assertEquals(123456789, exactCurrencyAmount.getUnscaledValue());
        assertEquals(5, exactCurrencyAmount.getScale());
    }
}
