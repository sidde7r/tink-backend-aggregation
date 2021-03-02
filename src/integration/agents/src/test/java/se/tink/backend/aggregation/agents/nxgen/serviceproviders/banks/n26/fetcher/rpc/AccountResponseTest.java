package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.transactionalaccount.N26AccountFetcherTestData;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AccountResponseTest {

    @Test
    public void toTransactionalAccount() {
        // given
        AccountResponse accountResponse = N26AccountFetcherTestData.fetchAccountsResponse();

        // when
        TransactionalAccount result = accountResponse.toTransactionalAccount();

        // then
        assertThat(result).isInstanceOf(TransactionalAccount.class);
        assertThat(result.getAccountNumber()).isEqualTo("DE95100110016601026293");
        assertThat(result.getName()).isEqualTo("N26 Bank");
        assertThat(result.getExactBalance().compareTo(ExactCurrencyAmount.inEUR(100.00)))
                .isEqualTo(0);
        // and identifiers
        List<AccountIdentifier> accountIdentifiers = result.getIdentifiers();
        assertThat(accountIdentifiers.size()).isEqualTo(1);
        assertThat(accountIdentifiers.get(0)).isInstanceOf(IbanIdentifier.class);
        assertThat(accountIdentifiers.get(0).getIdentifier()).isEqualTo("DE95100110016601026293");
        // and
        assertThat(result.isUniqueIdentifierEqual("DE95100110016601026293")).isTrue();
    }
}
