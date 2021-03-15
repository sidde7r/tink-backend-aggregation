package se.tink.backend.aggregation.nxgen.core.account.transactional;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.OtherIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionalAccountTest {

    private static final String ACCOUNT_NUMBER = "123456";

    @Test
    public void ensureBankIdentifierHasCorrectFormat() {
        TransactionalAccount transactionalAccount =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.CHECKING)
                        .withoutFlags()
                        .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(123.45)))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier("UN123")
                                        .withAccountNumber("AN123")
                                        .withAccountName("NM123")
                                        .addIdentifier(new OtherIdentifier("ID123"))
                                        .build())
                        .setApiIdentifier(ACCOUNT_NUMBER)
                        .build()
                        .orElse(null);

        assertEquals(ACCOUNT_NUMBER, transactionalAccount.getApiIdentifier());
    }
}
