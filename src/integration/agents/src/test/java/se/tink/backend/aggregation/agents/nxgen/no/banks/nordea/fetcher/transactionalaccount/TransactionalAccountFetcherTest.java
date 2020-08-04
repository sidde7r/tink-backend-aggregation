package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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

    private static final String ACCOUNTS_DATA_JSON =
            "{\"result\": [{\"account_id\": \"86011117947\", \"account_status\": \"open\", \"available_balance\": 1801.4, \"bic\": \"NDEANOKK\", \"booked_balance\": 1801.4, \"category\": \"transaction\", \"country_code\": \"NO\", \"credit_limit\": 22.0, \"currency\": \"NOK\", \"display_account_number\": \"8601.11.17947\", \"equivalent_balance\": 1801.4, \"equivalent_currency\": \"NOK\", \"iban\": \"NO9386011117947\", \"nickname\": \"Felleskonto\", \"permissions\": {\"can_deposit_to_account\": true, \"can_pay_from_account\": true, \"can_transfer_from_account\": true, \"can_transfer_to_account\": true, \"can_view\": true, \"can_view_transactions\": true }, \"product_code\": \"FORP\", \"product_name\": \"Brukskonto\", \"product_type\": \"KKT\", \"roles\": [{\"name\": \"GURO LARSEN ASDF\", \"role\": \"power_of_attorney\"}, {\"name\": \"First Second Surname\", \"role\": \"owner\"} ], \"statement_format\": \"electronic\", \"transaction_list_search_criteria\": {\"can_use_end_date\": true, \"can_use_free_text\": false, \"can_use_highest_amount\": false, \"can_use_lowest_amount\": false, \"can_use_start_date\": true } }, {\"account_id\": \"86022227947\", \"account_status\": \"open\", \"available_balance\": 102.22, \"bic\": \"NDEANOKK\", \"booked_balance\": 401.22, \"category\": \"savings\", \"country_code\": \"NO\", \"credit_limit\": 0.0, \"currency\": \"NOK\", \"display_account_number\": \"8602.22.27947\", \"equivalent_balance\": 1801.4, \"equivalent_currency\": \"NOK\", \"iban\": \"NO9386022227947\", \"nickname\": \"Felleskonto11111\", \"permissions\": {\"can_deposit_to_account\": true, \"can_pay_from_account\": true, \"can_transfer_from_account\": true, \"can_transfer_to_account\": true, \"can_view\": true, \"can_view_transactions\": true }, \"product_code\": \"FORP\", \"product_name\": \"Brukskonto123\", \"product_type\": \"KKT\", \"roles\": [{\"name\": \"GURO LARSEN ASDF\", \"role\": \"power_of_attorney\"}, {\"name\": \"First Second Surname\", \"role\": \"owner\"} ], \"statement_format\": \"electronic\", \"transaction_list_search_criteria\": {\"can_use_end_date\": true, \"can_use_free_text\": false, \"can_use_highest_amount\": false, \"can_use_lowest_amount\": false, \"can_use_start_date\": true } } ] }";

    private static final String ACCOUNTS_NOT_TRANSACTIONAL_DATA_JSON =
            "{\"result\": [{\"account_id\": \"86011117947\", \"account_status\": \"open\", \"available_balance\": 1801.4, \"bic\": \"NDEANOKK\", \"booked_balance\": 1801.4, \"category\": \"jibberish\", \"country_code\": \"NO\", \"credit_limit\": 22.0, \"currency\": \"NOK\", \"display_account_number\": \"8601.11.17947\", \"equivalent_balance\": 1801.4, \"equivalent_currency\": \"NOK\", \"iban\": \"NO9386011117947\", \"nickname\": \"Felleskonto\", \"permissions\": {\"can_deposit_to_account\": true, \"can_pay_from_account\": true, \"can_transfer_from_account\": true, \"can_transfer_to_account\": true, \"can_view\": true, \"can_view_transactions\": true }, \"product_code\": \"FORP\", \"product_name\": \"Brukskonto\", \"product_type\": \"KKT\", \"roles\": [{\"name\": \"GURO LARSEN ASDF\", \"role\": \"power_of_attorney\"}, {\"name\": \"First Second Surname\", \"role\": \"owner\"} ], \"statement_format\": \"electronic\", \"transaction_list_search_criteria\": {\"can_use_end_date\": true, \"can_use_free_text\": false, \"can_use_highest_amount\": false, \"can_use_lowest_amount\": false, \"can_use_start_date\": true } }, {\"account_id\": \"86022227947\", \"account_status\": \"open\", \"available_balance\": 102.22, \"bic\": \"NDEANOKK\", \"booked_balance\": 401.22, \"category\": \"jibberish\", \"country_code\": \"NO\", \"credit_limit\": 0.0, \"currency\": \"NOK\", \"display_account_number\": \"8602.22.27947\", \"equivalent_balance\": 1801.4, \"equivalent_currency\": \"NOK\", \"iban\": \"NO9386022227947\", \"nickname\": \"Felleskonto11111\", \"permissions\": {\"can_deposit_to_account\": true, \"can_pay_from_account\": true, \"can_transfer_from_account\": true, \"can_transfer_to_account\": true, \"can_view\": true, \"can_view_transactions\": true }, \"product_code\": \"FORP\", \"product_name\": \"Brukskonto123\", \"product_type\": \"KKT\", \"roles\": [{\"name\": \"GURO LARSEN ASDF\", \"role\": \"power_of_attorney\"}, {\"name\": \"First Second Surname\", \"role\": \"owner\"} ], \"statement_format\": \"electronic\", \"transaction_list_search_criteria\": {\"can_use_end_date\": true, \"can_use_free_text\": false, \"can_use_highest_amount\": false, \"can_use_lowest_amount\": false, \"can_use_start_date\": true } } ] }";

    @Test
    public void shouldReturnProperlyMappedAccounts() {
        // given
        FetcherClient fetcherClient = mock(FetcherClient.class);
        TransactionalAccountFetcher transactionalAccountFetcher =
                new TransactionalAccountFetcher(fetcherClient);

        given(fetcherClient.fetchAccounts())
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                ACCOUNTS_DATA_JSON, AccountsResponse.class));
        // when
        Collection<TransactionalAccount> transactionalAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(transactionalAccounts).hasSize(2);
        Iterator<TransactionalAccount> iterator = transactionalAccounts.iterator();
        assertThatAccountIsProperlyMapped(
                iterator.next(),
                AccountTypes.CHECKING,
                "86011117947",
                ExactCurrencyAmount.of(1801.4, "NOK"),
                ExactCurrencyAmount.of(1801.4, "NOK"),
                ExactCurrencyAmount.of(22.0, "NOK"),
                "NO9386011117947",
                "Brukskonto",
                "Felleskonto");
        assertThatAccountIsProperlyMapped(
                iterator.next(),
                AccountTypes.SAVINGS,
                "86022227947",
                ExactCurrencyAmount.of(401.22, "NOK"),
                ExactCurrencyAmount.of(102.22, "NOK"),
                ExactCurrencyAmount.of(0.0, "NOK"),
                "NO9386022227947",
                "Brukskonto123",
                "Felleskonto11111");
    }

    private void assertThatAccountIsProperlyMapped(
            TransactionalAccount account,
            AccountTypes expectedType,
            String expectedApiIdentifier,
            ExactCurrencyAmount expectedBookedBalance,
            ExactCurrencyAmount expectedAvailableBalance,
            ExactCurrencyAmount expectedCreditLimit,
            String expectedIban,
            String expectedProductName,
            String expectedAccountName) {
        assertThat(account.getType()).isEqualTo(expectedType);
        assertThat(account.getApiIdentifier()).isEqualTo(expectedApiIdentifier);
        assertThat(account.getHolders()).hasSize(1);
        assertThat(account.getHolders().get(0)).isEqualTo(Holder.of("First Second Surname"));

        assertThat(account.getExactBalance()).isEqualTo(expectedBookedBalance);
        assertThat(account.getExactAvailableBalance()).isEqualTo(expectedAvailableBalance);
        assertThat(account.getExactCreditLimit()).isEqualTo(expectedCreditLimit);

        assertThat(account.isUniqueIdentifierEqual(expectedIban)).isTrue();

        assertThat(account.getAccountNumber()).isEqualTo(expectedIban);
        assertThat(account.getIdModule().getProductName()).isEqualTo(expectedProductName);
        assertThat(account.getName()).isEqualTo(expectedAccountName);
        assertThat(account.getIdentifiers())
                .containsExactlyInAnyOrder(
                        new NorwegianIdentifier(expectedApiIdentifier),
                        new IbanIdentifier("NDEANOKK", expectedIban));
    }

    @Test
    public void shouldFilterOutNonTransactionalAccounts() {
        // given
        FetcherClient fetcherClient = mock(FetcherClient.class);
        TransactionalAccountFetcher transactionalAccountFetcher =
                new TransactionalAccountFetcher(fetcherClient);

        given(fetcherClient.fetchAccounts())
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                ACCOUNTS_NOT_TRANSACTIONAL_DATA_JSON, AccountsResponse.class));

        // when
        Collection<TransactionalAccount> transactionalAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(transactionalAccounts).isEmpty();
    }
}
