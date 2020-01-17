package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.util;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.NorwegianIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CustomAsserts {

    public static void assertTransactionalAccountsEqual(
            TransactionalAccount account,
            AccountTypes accountType,
            ExactCurrencyAmount balance,
            String uniqueIdentifier,
            String accountNumber,
            String accountName,
            String iban,
            String bban,
            String apiIdentifier,
            String holderName,
            String transactionLink) {
        assertEquals(accountType, account.getType());
        assertEquals(balance, account.getExactBalance());
        assertTrue(
                "Unique identifier does not match!",
                account.isUniqueIdentifierEqual(uniqueIdentifier));
        assertEquals(accountNumber, account.getAccountNumber());
        assertEquals(accountName, account.getName());
        assertThat(account.getIdentifiers())
                .containsOnly(new IbanIdentifier(iban), new NorwegianIdentifier(bban));
        assertEquals(apiIdentifier, account.getApiIdentifier());
        assertEquals(holderName, account.getHolderName().toString());
        assertEquals(
                transactionLink,
                account.getFromTemporaryStorage(SparebankConstants.StorageKeys.TRANSACTIONS_URL));
    }
}
