package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.strings.StringUtils;
import static org.junit.Assert.*;

public class CreditCardEntityTest {

    private CreditCardEntity entity;
    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
    }

    @Test
    public void toTinkCreditCard() throws IOException {
        entity = mapper.readValue(CreditCardEntityTestData.CREDIT_CARD_DATA, CreditCardEntity.class);
        CreditCardAccount creditCardAccount = entity.toTinkCreditCard();

        assertTrue(creditCardAccount.isUniqueIdentifierEqual(CreditCardEntityTestData.UNIQUE_ID));
        assertEquals(CreditCardEntityTestData.AMOUNT, creditCardAccount.getBalance());
        assertEquals(CreditCardEntityTestData.NAME, creditCardAccount.getName());

    }
}