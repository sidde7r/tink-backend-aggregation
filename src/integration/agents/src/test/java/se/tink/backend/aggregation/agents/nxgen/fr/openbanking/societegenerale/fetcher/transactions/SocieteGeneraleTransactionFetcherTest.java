package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class SocieteGeneraleTransactionFetcherTest {

    @Mock private SocieteGeneraleApiClient apiClient;
    private static final URL NEXT_PAGE_URL = new URL("someUrl");
    private static final String API_ID = "apiIdentifier";

    @Test
    public void shouldGetTransactionsForCheckingAccount() {
        // given
        TransactionalAccount account = mock(TransactionalAccount.class);

        when(account.getApiIdentifier()).thenReturn(API_ID);
        when(apiClient.getTransactions(anyString(), any()))
                .thenReturn(getCheckingAccountsTransactionResponse());

        SocieteGeneraleTransactionFetcher<TransactionalAccount> societeGeneraleTransactionFetcher =
                new SocieteGeneraleTransactionFetcher<>(apiClient);

        // when
        TransactionKeyPaginatorResponse<URL> response =
                societeGeneraleTransactionFetcher.getTransactionsFor(account, NEXT_PAGE_URL);

        // then
        assertThat(response.canFetchMore().get()).isTrue();
        List<Transaction> transactions = new ArrayList<>(response.getTinkTransactions());
        assertThat(transactions).hasSize(3);

        Transaction transaction1 = transactions.get(0);
        assertThat(transaction1.getDescription()).isEqualTo("some description 1");
        assertThat(transaction1.getAmount().getDoubleValue()).isEqualTo(50.00);
        assertThat(transaction1.isPending()).isFalse();
        assertThat(transaction1.getDate()).isEqualToIgnoringHours("2021-10-14");

        Transaction transaction2 = transactions.get(1);
        assertThat(transaction2.getDescription()).isEqualTo("some description 2");
        assertThat(transaction2.getAmount().getDoubleValue()).isEqualTo(-12.34);
        assertThat(transaction2.isPending()).isFalse();
        assertThat(transaction2.getDate()).isEqualToIgnoringHours("2021-09-02");

        Transaction transaction3 = transactions.get(2);
        assertThat(transaction3.getDescription()).isEqualTo("some description 3");
        assertThat(transaction3.getAmount().getDoubleValue()).isEqualTo(-6.49);
        assertThat(transaction3.isPending()).isTrue();
        assertThat(transaction3.getDate()).isEqualToIgnoringHours("2021-11-02");
    }

    @Test
    public void shouldGetTransactionsForCreditCard() {
        // given
        CreditCardAccount account = mock(CreditCardAccount.class);

        when(account.getApiIdentifier()).thenReturn(API_ID);
        when(apiClient.getTransactions(anyString(), any()))
                .thenReturn(getCreditCardTransactionResponse());

        SocieteGeneraleTransactionFetcher<CreditCardAccount> societeGeneraleTransactionFetcher =
                new SocieteGeneraleTransactionFetcher<>(apiClient);

        // when
        TransactionKeyPaginatorResponse<URL> response =
                societeGeneraleTransactionFetcher.getTransactionsFor(account, NEXT_PAGE_URL);

        // then
        assertThat(response.canFetchMore().get()).isTrue();
        List<Transaction> transactions = new ArrayList<>(response.getTinkTransactions());
        assertThat(transactions).hasSize(2);
        Transaction transaction1 = transactions.get(0);
        assertThat(transaction1.getDescription()).isEqualTo("some description 1");
        assertThat(transaction1.getAmount().getDoubleValue()).isEqualTo(-5.45);
        assertThat(transaction1.isPending()).isTrue();
        assertThat(transaction1.getDate()).isEqualToIgnoringHours("2021-11-04");

        Transaction transaction2 = transactions.get(1);
        assertThat(transaction2.getDescription()).isEqualTo("some description 2");
        assertThat(transaction2.getAmount().getDoubleValue()).isEqualTo(-17.05);
        assertThat(transaction2.isPending()).isFalse();
        assertThat(transaction2.getDate()).isEqualToIgnoringHours("2021-09-02");
    }

    private TransactionsResponse getCheckingAccountsTransactionResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"transactions\": [\n"
                        + "    {\n"
                        + "      \"entryReference\": \"2021-10-14T00:001036176\",\n"
                        + "      \"transactionAmount\": {\n"
                        + "        \"currency\": \"EUR\",\n"
                        + "        \"amount\": \"50.00\"\n"
                        + "      },\n"
                        + "      \"creditDebitIndicator\": \"CRDT\",\n"
                        + "      \"status\": \"BOOK\",\n"
                        + "      \"bookingDate\": \"2021-10-14\",\n"
                        + "      \"valueDate\": \"2021-10-14\",\n"
                        + "      \"remittanceInformation\": {\n"
                        + "        \"unstructured\": [\n"
                        + "          \"some description 1\"\n"
                        + "        ]\n"
                        + "      }\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"entryReference\": \"2021-09-02T00:002034503\",\n"
                        + "      \"transactionAmount\": {\n"
                        + "        \"currency\": \"EUR\",\n"
                        + "        \"amount\": \"12.34\"\n"
                        + "      },\n"
                        + "      \"creditDebitIndicator\": \"DBIT\",\n"
                        + "      \"status\": \"BOOK\",\n"
                        + "      \"bookingDate\": \"2021-09-02\",\n"
                        + "      \"valueDate\": \"2021-09-02\",\n"
                        + "      \"remittanceInformation\": {\n"
                        + "        \"unstructured\": [\n"
                        + "          \"some description 2\"\n"
                        + "        ]\n"
                        + "      }\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"transactionAmount\": {\n"
                        + "        \"currency\": \"EUR\",\n"
                        + "        \"amount\": \"6.49\"\n"
                        + "      },\n"
                        + "      \"creditDebitIndicator\": \"DBIT\",\n"
                        + "      \"status\": \"PDNG\",\n"
                        + "      \"bookingDate\": \"2021-11-02\",\n"
                        + "      \"valueDate\": \"2021-11-02\",\n"
                        + "      \"remittanceInformation\": {\n"
                        + "        \"unstructured\": [\n"
                        + "          \"some description 3\"\n"
                        + "        ]\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"_links\": {\n"
                        + "    \"self\": {\n"
                        + "      \"href\": \"/accounts/resourceId/transactions?page\\u003d1\",\n"
                        + "      \"templated\": true\n"
                        + "    },\n"
                        + "    \"parent-list\": {\n"
                        + "      \"href\": \"/accounts\"\n"
                        + "    },\n"
                        + "    \"balances\": {\n"
                        + "      \"href\": \"/accounts/resourceId/balances\"\n"
                        + "    },\n"
                        + "    \"first\": {\n"
                        + "      \"href\": \"/accounts/resourceId/transactions?page\\u003d1\",\n"
                        + "      \"templated\": true\n"
                        + "    },\n"
                        + "    \"last\": {\n"
                        + "      \"href\": \"/accounts/resourceId/transactions?page\\u003d4\",\n"
                        + "      \"templated\": true\n"
                        + "    },\n"
                        + "    \"next\": {\n"
                        + "      \"href\": \"/accounts/resourceId/transactions?page\\u003d2\",\n"
                        + "      \"templated\": true\n"
                        + "    }\n"
                        + "  }\n"
                        + "}",
                TransactionsResponse.class);
    }

    private TransactionsResponse getCreditCardTransactionResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"transactions\": [\n"
                        + "    {\n"
                        + "      \"entryReference\": \"entryReference1\",\n"
                        + "      \"transactionAmount\": {\n"
                        + "        \"currency\": \"EUR\",\n"
                        + "        \"amount\": \"5.45\"\n"
                        + "      },\n"
                        + "      \"creditDebitIndicator\": \"DBIT\",\n"
                        + "      \"status\": \"OTHR\",\n"
                        + "      \"expectingBookingDate\": \"2021-11-04\",\n"
                        + "      \"valueDate\": \"2021-10-14\",\n"
                        + "      \"remittanceInformation\": {\n"
                        + "        \"unstructured\": [\n"
                        + "          \"some description 1\"\n"
                        + "        ]\n"
                        + "      }\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"entryReference\": \"entryReference2\",\n"
                        + "      \"transactionAmount\": {\n"
                        + "        \"currency\": \"EUR\",\n"
                        + "        \"amount\": \"17.05\"\n"
                        + "      },\n"
                        + "      \"creditDebitIndicator\": \"DBIT\",\n"
                        + "      \"status\": \"BOOK\",\n"
                        + "      \"bookingDate\": \"2021-09-02\",\n"
                        + "      \"valueDate\": \"2021-08-18\",\n"
                        + "      \"remittanceInformation\": {\n"
                        + "        \"unstructured\": [\n"
                        + "          \"some description 2\"\n"
                        + "        ]\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"_links\": {\n"
                        + "    \"self\": {\n"
                        + "      \"href\": \"/accounts/resourceId/transactions?page\\u003d1\",\n"
                        + "      \"templated\": true\n"
                        + "    },\n"
                        + "    \"parent-list\": {\n"
                        + "      \"href\": \"/accounts\"\n"
                        + "    },\n"
                        + "    \"balances\": {\n"
                        + "      \"href\": \"/accounts/resourceId/balances\"\n"
                        + "    },\n"
                        + "    \"first\": {\n"
                        + "      \"href\": \"/accounts/resourceId/transactions?page\\u003d1\\u0026beforeEntryReference\\u003dentryReference1\",\n"
                        + "      \"templated\": true\n"
                        + "    },\n"
                        + "    \"last\": {\n"
                        + "      \"href\": \"/accounts/resourceId/transactions?page\\u003d5\\u0026beforeEntryReference\\u003dentryReference1\",\n"
                        + "      \"templated\": true\n"
                        + "    },\n"
                        + "    \"next\": {\n"
                        + "      \"href\": \"/accounts/resourceId/transactions?page\\u003d2\\u0026beforeEntryReference\\u003dentryReference1\",\n"
                        + "      \"templated\": true\n"
                        + "    }\n"
                        + "  }\n"
                        + "}",
                TransactionsResponse.class);
    }
}
