package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.NorwegianIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionalAccountFetcherTest {
    private static final String ACCOUNTS_DATA_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/resources/transactionalAccounts.json";

    private static final String ACCOUNTS_NOT_TRANSACTIONAL_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/resources/notTransactionalAccounts.json";

    private static final AccountsResponse ACCOUNTS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(ACCOUNTS_DATA_FILE_PATH), AccountsResponse.class);
    private static final AccountsResponse ACCOUNTS_NOT_TRANSACTIONAL_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(ACCOUNTS_NOT_TRANSACTIONAL_FILE_PATH), AccountsResponse.class);

    @Test
    public void shouldReturnProperlyMappedAccounts() {
        // given
        FetcherClient fetcherClient = mock(FetcherClient.class);
        TransactionalAccountFetcher transactionalAccountFetcher =
                new TransactionalAccountFetcher(fetcherClient);

        given(fetcherClient.fetchAccounts()).willReturn(ACCOUNTS_RESPONSE);
        // when
        Collection<TransactionalAccount> transactionalAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(transactionalAccounts).hasSize(2);
        Iterator<TransactionalAccount> iterator = transactionalAccounts.iterator();
        TransactionalAccount account = iterator.next();
        assertThatAccountIsProperlyMapped(
                account,
                AccountTypes.CHECKING,
                "86011117947",
                "NO9386011117947",
                "Brukskonto",
                "Felleskonto");
        assertThatBalancesAreProperlyMapped(
                account,
                ExactCurrencyAmount.of(1801.4, "NOK"),
                ExactCurrencyAmount.of(1801.4, "NOK"),
                ExactCurrencyAmount.of(22.0, "NOK"));

        account = iterator.next();
        assertThatAccountIsProperlyMapped(
                account,
                AccountTypes.SAVINGS,
                "86022227947",
                "NO9386022227947",
                "Brukskonto123",
                "Felleskonto11111");
        assertThatBalancesAreProperlyMapped(
                account,
                ExactCurrencyAmount.of(401.22, "NOK"),
                ExactCurrencyAmount.of(102.22, "NOK"),
                ExactCurrencyAmount.zero("NOK"));
    }

    private void assertThatAccountIsProperlyMapped(
            TransactionalAccount account,
            AccountTypes expectedType,
            String expectedApiIdentifier,
            String expectedIban,
            String expectedProductName,
            String expectedAccountName) {
        assertThat(account.getType()).isEqualTo(expectedType);
        assertThat(account.getApiIdentifier()).isEqualTo(expectedApiIdentifier);
        assertThat(account.getHolders()).hasSize(1);
        assertThat(account.getHolders().get(0)).isEqualTo(Holder.of("First Second Surname"));

        assertThat(account.isUniqueIdentifierEqual(expectedIban)).isTrue();

        assertThat(account.getAccountNumber()).isEqualTo(expectedIban);
        assertThat(account.getIdModule().getProductName()).isEqualTo(expectedProductName);
        assertThat(account.getName()).isEqualTo(expectedAccountName);
        assertThat(account.getIdentifiers())
                .containsExactlyInAnyOrder(
                        new NorwegianIdentifier(expectedApiIdentifier),
                        new IbanIdentifier("NDEANOKK", expectedIban));
    }

    private void assertThatBalancesAreProperlyMapped(
            TransactionalAccount account,
            ExactCurrencyAmount expectedBookedBalance,
            ExactCurrencyAmount expectedAvailableBalance,
            ExactCurrencyAmount expectedCreditLimit) {
        assertThat(account.getExactBalance()).isEqualTo(expectedBookedBalance);
        assertThat(account.getExactAvailableBalance()).isEqualTo(expectedAvailableBalance);
        assertThat(account.getExactCreditLimit()).isEqualTo(expectedCreditLimit);
    }

    @Test
    public void shouldFilterOutNonTransactionalAccounts() {
        // given
        FetcherClient fetcherClient = mock(FetcherClient.class);
        TransactionalAccountFetcher transactionalAccountFetcher =
                new TransactionalAccountFetcher(fetcherClient);

        given(fetcherClient.fetchAccounts()).willReturn(ACCOUNTS_NOT_TRANSACTIONAL_RESPONSE);

        // when
        Collection<TransactionalAccount> transactionalAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(transactionalAccounts).isEmpty();
    }
}
