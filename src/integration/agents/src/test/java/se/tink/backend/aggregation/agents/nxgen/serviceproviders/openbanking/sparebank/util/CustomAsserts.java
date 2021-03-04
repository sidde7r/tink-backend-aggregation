package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.util;

import static org.assertj.core.api.Assertions.assertThat;

import se.tink.backend.agents.rpc.AccountTypes;
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
            String transactionLink) {
        assertThat(account.getType()).isEqualTo(accountType);
        assertThat(account.getExactBalance()).isEqualTo(balance);
        assertThat(account.isUniqueIdentifierEqual(uniqueIdentifier)).isTrue();
        assertThat(account.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(account.getName()).isEqualTo(accountName);
        assertThat(account.getIdentifiers())
                .containsOnly(new IbanIdentifier(iban), new NorwegianIdentifier(bban));
        assertThat(account.getApiIdentifier()).isEqualTo(apiIdentifier);
    }
}
