package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v62.AmericanExpressV62SEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class AmericanExpressV62PredicatesTest {

    private ObjectMapper mapper = new ObjectMapper();
    private CardEntity mainCardEntity;
    private CreditCardAccount mainCard;

    @Before
    public void setUp() throws Exception {
        mainCardEntity =
                mapper.readValue(AmericanExpressV62PredicatesTestData.MAIN_CARD, CardEntity.class);
        mainCard = mainCardEntity.toCreditCardAccount(new AmericanExpressV62SEConfiguration());
    }

    @Test
    public void getCardEndingNumber_fromCardEntity() {
        assertEquals(
                AmericanExpressV62Predicates.getCardEndingNumbers.apply(
                        mainCardEntity.getCardNumberDisplay()),
                AmericanExpressV62PredicatesTestData.CARD_NUMBER);
    }

    @Test
    public void getCardEndingNumber_fromCreditCardAccount() {
        assertEquals(
                AmericanExpressV62Predicates.getCardEndingNumbers.apply(
                        mainCard.getAccountNumber()),
                AmericanExpressV62PredicatesTestData.CARD_NUMBER);
    }
}
