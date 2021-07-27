package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role.HOLDER;
import static se.tink.libraries.account.enums.AccountIdentifierType.IBAN;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.TestDataReader;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class GetAccountsResponseTest {

    private static final int NO_OF_ACCOUNTS_IN_RESPONSE = 3;
    private static final int NO_OF_ACCOUNTS_AFTER_FILTERING_LOANS = 2;

    @Test
    public void shouldReturnCorrectTinkAccounts() {
        // given
        GetAccountsResponse getAccountsResponse =
                TestDataReader.readFromFile(
                        TestDataReader.ACCOUNTS_RESP, GetAccountsResponse.class);

        // when
        List<TransactionalAccount> tinkAccounts = getAccountsResponse.getTinkAccounts();

        // then
        assertThat(getAccountsResponse.getAccounts()).hasSize(NO_OF_ACCOUNTS_IN_RESPONSE);
        assertThat(tinkAccounts).hasSize(NO_OF_ACCOUNTS_AFTER_FILTERING_LOANS);

        verifyCheckingAccount(tinkAccounts.get(0));
        verifySavingsAccount(tinkAccounts.get(1));
    }

    private static void verifyCheckingAccount(TransactionalAccount account) {
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getName()).isEqualTo("[CHECKING ACCOUNT] Basiskonto");
        assertThat(account.isUniqueIdentifierEqual("DK0950519437252524")).isTrue();
        assertThat(account.getAccountNumber()).isEqualTo("9437252524");

        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.inDKK(11.0));
        assertThat(account.getExactAvailableBalance()).isNull();

        assertThat(account.getParties()).containsExactly(new Party("Account Owner 1", HOLDER));
        verifyIdentifiers(account, singletonMap(IBAN, "RINGDK11/DK0950519437252524"));
    }

    private static void verifySavingsAccount(TransactionalAccount account) {
        assertThat(account.getType()).isEqualTo(AccountTypes.SAVINGS);
        assertThat(account.getName()).isEqualTo("[SAVINGS ACCOUNT] OpSpaRinG");
        assertThat(account.isUniqueIdentifierEqual("DK8150519154354414")).isTrue();
        assertThat(account.getAccountNumber()).isEqualTo("9154354414");

        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.inDKK(12.0));
        assertThat(account.getExactAvailableBalance()).isNull();

        assertThat(account.getParties()).containsExactly(new Party("Account Owner 2", HOLDER));
        verifyIdentifiers(account, singletonMap(IBAN, "RINGDK22/DK8150519154354414"));
    }

    private static void verifyIdentifiers(
            TransactionalAccount account, Map<AccountIdentifierType, String> expectedIdentifiers) {
        Map<AccountIdentifierType, String> actualIdentifiers =
                account.getIdentifiers().stream()
                        .collect(
                                Collectors.toMap(
                                        AccountIdentifier::getType,
                                        AccountIdentifier::getIdentifier));
        assertThat(actualIdentifiers).containsExactlyInAnyOrderEntriesOf(expectedIdentifiers);
    }
}
