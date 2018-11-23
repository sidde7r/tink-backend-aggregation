package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.SubcardEntity;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.core.Amount;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class AmericanExpressV62PredicatesTest {
    private ObjectMapper mapper = new ObjectMapper();
    private SubcardEntity cardEntity;

    @Before
    public void setUp() throws Exception {
        cardEntity =
                mapper.readValue(
                        AmericanExpressV62PredicatesTestData.CARD_ENTITY, SubcardEntity.class);
    }

    @Test
    public void compareCredidEntityToAccount_true() {
        CreditCardAccount account =
                CreditCardAccount.builder("123")
                        .setAccountNumber("XXXX-12345")
                        .setBalance(Amount.inEUR(10))
                        .setAvailableCredit(Amount.inEUR(0))
                        .build();

        boolean test =
                AmericanExpressV62Predicates.compareCardEntityToAccount.test(cardEntity, account);
        assertTrue(test);
    }

    @Test
    public void compareCredidEntityToAccount_false() {
        CreditCardAccount account =
                CreditCardAccount.builder("123")
                        .setAccountNumber("XXXX-54321")
                        .setBalance(Amount.inEUR(10))
                        .setAvailableCredit(Amount.inEUR(0))
                        .build();

        boolean test =
                AmericanExpressV62Predicates.compareCardEntityToAccount.test(cardEntity, account);
        assertFalse(test);
    }
}
