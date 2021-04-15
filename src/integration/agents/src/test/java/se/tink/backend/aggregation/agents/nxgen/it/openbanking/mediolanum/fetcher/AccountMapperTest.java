package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AccountMapperTest {

    private static final String TEST_IBAN = "IT98V1234567890123456789012";

    @Test
    public void shouldMapAccountProperly() {
        // given
        AccountEntity accountEntity =
                TestDataReader.readFromFile(TestDataReader.TWO_ACCOUNTS, AccountsResponse.class)
                        .getAccounts()
                        .get(0);
        AccountMapper accountMapper = new AccountMapper();

        // when
        Optional<TransactionalAccount> transactionalAccount =
                accountMapper.toTinkAccount(accountEntity);

        // then
        assertThat(transactionalAccount.isPresent()).isTrue();
        TransactionalAccount account = transactionalAccount.get();

        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(1234, "EUR"));
        assertThat(account.getExactAvailableBalance())
                .isEqualTo(ExactCurrencyAmount.of(2345, "EUR"));
        assertThat(account.isUniqueIdentifierEqual(TEST_IBAN)).isTrue();
        assertThat(account.getIdModule().getAccountNumber()).isEqualTo(TEST_IBAN);
        assertThat(account.getIdModule().getAccountName()).isEqualTo(TEST_IBAN);
        assertThat(account.getIdModule().getIdentifiers()).hasSize(1);
        assertThat(account.getIdModule().getIdentifiers()).contains(new IbanIdentifier(TEST_IBAN));
        assertThat(account.getApiIdentifier()).isEqualTo(TEST_IBAN + "_EUR");
    }
}
