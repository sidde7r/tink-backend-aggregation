package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IngBaseCardTransactionsFetcherTest {

    private final FetchCardTransactionsResponse TEST_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{"
                            + "  \"cardAccount\": ["
                            + "    {"
                            + "      \"maskedPan\": \"12345******1234\","
                            + "      \"currency\": \"EUR\""
                            + "    }"
                            + "  ],"
                            + "  \"cardTransactions\": {"
                            + "    \"booked\": ["
                            + "      {"
                            + "        \"cardTransactionId\": \"trx123456789\","
                            + "        \"transactionDate\": \"2017-11-20\","
                            + "        \"bookingDate\": \"2017-11-20\","
                            + "        \"transactionAmount\": {"
                            + "          \"currency\": \"EUR\","
                            + "          \"amount\": 100.12"
                            + "        },"
                            + "        \"maskedPan\": \"12345******1234\","
                            + "        \"transactionDetails\": \"DESCRIPTION\""
                            + "      }"
                            + "    ],"
                            + "    \"pending\": ["
                            + "      {"
                            + "        \"cardTransactionId\": \"trx45678921\","
                            + "        \"transactionDate\": \"2017-11-21\","
                            + "        \"transactionAmount\": {"
                            + "          \"currency\": \"EUR\","
                            + "          \"amount\": 55.98"
                            + "        },"
                            + "        \"maskedPan\": \"12345******1234\","
                            + "        \"transactionDetails\": \"DESCRIPTION\""
                            + "      }"
                            + "    ],"
                            + "    \"_links\": {"
                            + "      \"next\": {"
                            + "        \"href\": \"/v1/card-accounts/7de0041d-4f25-4b6c-a885-0bbeb1eab220/transactions?next=CQR23TABC\""
                            + "      }"
                            + "    }"
                            + "  }"
                            + "}",
                    FetchCardTransactionsResponse.class);

    private final IngBaseApiClient apiClient = mock(IngBaseApiClient.class);

    private final IngBaseCardTransactionsFetcher fetcher =
            new IngBaseCardTransactionsFetcher(apiClient, LocalDate::now);

    @Test
    public void shouldFetchAndMapTransactionsToTinkModel() {
        // given
        CreditCardAccount creditCardAccount = mock(CreditCardAccount.class);
        given(apiClient.fetchCardTransactionsPage(anyString())).willReturn(TEST_RESPONSE);

        // when
        TransactionKeyPaginatorResponse<String> result =
                fetcher.getTransactionsFor(creditCardAccount, "key");

        // then
        Iterator<? extends Transaction> iterator = result.getTinkTransactions().iterator();
        Transaction booked = iterator.next();
        assertThat(booked.getDescription()).isEqualTo("DESCRIPTION");
        assertThat(booked.getDate().toString()).isEqualTo("Mon Nov 20 11:00:00 UTC 2017");
        assertThat(booked.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(100.12));
        assertThat(booked.isPending()).isFalse();

        Transaction pending = iterator.next();
        assertThat(pending.getDescription()).isEqualTo("DESCRIPTION");
        assertThat(pending.getDate().toString()).isEqualTo("Tue Nov 21 11:00:00 UTC 2017");
        assertThat(pending.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(55.98));
        assertThat(pending.isPending()).isTrue();

        assertThat(result.canFetchMore()).isPresent().isEqualTo(Optional.of(true));
    }
}
