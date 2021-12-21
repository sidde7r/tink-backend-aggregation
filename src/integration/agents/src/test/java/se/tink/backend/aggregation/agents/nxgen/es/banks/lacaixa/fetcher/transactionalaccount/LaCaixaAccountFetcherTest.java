package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.DATE_REGEX_YYYYMMDD;
import static se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role.AUTHORIZED_USER;
import static se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role.HOLDER;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.AccountTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.ListHoldersResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LaCaixaAccountFetcherTest {
    private LaCaixaApiClient laCaixaApiClient;
    private LaCaixaAccountFetcher accountFetcher;

    private static final String FIRST_ACCOUNT_REFERENCE = "Qlf1";
    private static final String SECOND_ACCOUNT_REFERENCE = "Qlf2";
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/lacaixa/resources";
    private static final String ACC_REF = "4599851319407040";

    @Before
    public void setup() {
        laCaixaApiClient = mock(LaCaixaApiClient.class);
        accountFetcher = new LaCaixaAccountFetcher(laCaixaApiClient);
    }

    @Test
    public void shouldFetchAndMapAccountsWithHolders() {
        // given
        when(laCaixaApiClient.fetchAccountList())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                                ListAccountsResponse.class));
        // and
        when(laCaixaApiClient.fetchHolderList(FIRST_ACCOUNT_REFERENCE))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "holders_account_1_response.json")
                                        .toFile(),
                                ListHoldersResponse.class));
        // and
        when(laCaixaApiClient.fetchHolderList(SECOND_ACCOUNT_REFERENCE))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "holders_account_2_response.json")
                                        .toFile(),
                                ListHoldersResponse.class));

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // then
        TransactionalAccount firstAccountWithHolders =
                accounts.stream()
                        .filter(
                                account ->
                                        TransactionalAccountType.SAVINGS
                                                .toAccountType()
                                                .equals(account.getType()))
                        .findFirst()
                        .orElse(null);
        assertFirstAccountValid(firstAccountWithHolders);

        // and
        TransactionalAccount secondAccountWithHolders =
                accounts.stream()
                        .filter(
                                account ->
                                        TransactionalAccountType.CHECKING
                                                .toAccountType()
                                                .equals(account.getType()))
                        .findFirst()
                        .orElse(null);
        assertSecondAccountValid(secondAccountWithHolders);
    }

    private void assertFirstAccountValid(TransactionalAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("48.65"));
        assertThat(account.getIdModule().getAccountName()).isEqualTo("Chunga");
        assertThat(account.getParties().get(0).getName()).isEqualTo("JOHN DOE LOE");
        assertThat(account.getParties().get(0).getRole()).isEqualTo(HOLDER);
        assertThat(account.getParties().get(1).getName()).isEqualTo("JOHNNY DONNY LONNY");
        assertThat(account.getParties().get(1).getRole()).isEqualTo(AUTHORIZED_USER);
    }

    private void assertSecondAccountValid(TransactionalAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(account.getIdModule().getAccountName()).isEqualTo("imagin");
        assertThat(account.getParties().get(0).getName()).isEqualTo("ALICE DOE NOE");
        assertThat(account.getParties().get(0).getRole()).isEqualTo(HOLDER);
    }

    @Test
    public void shouldFetchAndMapTransactionsToTinkTransactions() {
        // given
        when(laCaixaApiClient.fetchNextAccountTransactions(ACC_REF, true))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_transactions_response.json")
                                        .toFile(),
                                AccountTransactionResponse.class));

        // when
        AccountTransactionResponse transactions =
                laCaixaApiClient.fetchNextAccountTransactions(ACC_REF, true);

        // then
        transactions.getTinkTransactions().forEach(transaction -> {
            LocalDate date = transaction.getTransactionDates().getDates().get(1).getValue().getDate();
            Boolean isValidTinkDate = null == date || date.toString().matches(DATE_REGEX_YYYYMMDD);
            assertThat(isValidTinkDate).isTrue();
        });
    }
}
