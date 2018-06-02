package se.tink.backend.aggregation.nxgen.core.account;

import org.junit.Test;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import static org.junit.Assert.*;

public class TransactionalAccountTest {

    @Test
    public void ensureBankIdentifierHasCorrectFormat() {
        TransactionalAccount transactionalAccount = TransactionalAccount
                .builder(AccountTypes.CHECKING, "123456", Amount.inSEK(1.0))
                .setBankIdentifier("123456")
                .build();

        assertEquals("123456", transactionalAccount.getBankIdentifier());
    }
}
