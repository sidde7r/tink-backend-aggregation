package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditCardTransactionFetcherTest {

    private static final String CARD_ID = "1234567";
    private static final String FIRST_PAGE_DATA_JSON =
            "{\"billing_period\": null, \"page\": 1, \"page_size\": 2, \"size\": 2, \"transactions\": [{\"amount\": -4000, \"booked\": false, \"booking_date\": null, \"currency\": \"NOK\", \"ecological_footprint\": null, \"exchange_rate\": null, \"invoice_status\": null, \"invoiced_date\": null, \"mcc\": null, \"merchant_city\": null, \"merchant_country\": null, \"original_amount\": null, \"original_currency\": null, \"title\": \"Revolut**0873*\", \"transaction_date\": \"2020-07-23\", \"transaction_id\": \"003802\", \"transaction_type\": null }, {\"amount\": -597.05, \"booked\": true, \"booking_date\": \"2020-07-17\", \"currency\": \"NOK\", \"ecological_footprint\": null, \"exchange_rate\": 1.0, \"invoice_status\": null, \"invoiced_date\": null, \"mcc\": null, \"merchant_city\": \"London\", \"merchant_country\": \"GB\", \"original_amount\": -597.05, \"original_currency\": \"NOK\", \"title\": \"CRV*ST1 46101 ASKIM      London       GB\", \"transaction_date\": \"2020-07-15\", \"transaction_id\": \"2019901320011576\", \"transaction_type\": null } ] }";
    private static final String SECOND_PAGE_DATA_JSON =
            "{\"billing_period\": null, \"page\": 2, \"page_size\": 2, \"size\": 1, \"transactions\": [{\"amount\": -173.5, \"booked\": true, \"booking_date\": \"2020-07-01\", \"currency\": \"NOK\", \"ecological_footprint\": null, \"exchange_rate\": 1.0, \"invoice_status\": null, \"invoiced_date\": null, \"mcc\": null, \"merchant_city\": \"Stathelle\", \"merchant_country\": \"NO\", \"original_amount\": -173.5, \"original_currency\": \"NOK\", \"title\": \"EUROSPAR RUGTVE          Stathelle    NO\", \"transaction_date\": \"2020-06-29\", \"transaction_id\": \"2018301320009896\", \"transaction_type\": null } ] }";

    @Test
    public void shouldReturnProperlyMappedCreditCardTransactions() {
        // given
        FetcherClient fetcherClient = mock(FetcherClient.class);
        given(fetcherClient.fetchCreditCardTransactions(CARD_ID, 1))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                FIRST_PAGE_DATA_JSON, CreditCardTransactionsResponse.class));
        given(fetcherClient.fetchCreditCardTransactions(CARD_ID, 2))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                SECOND_PAGE_DATA_JSON, CreditCardTransactionsResponse.class));
        CreditCardTransactionFetcher fetcher = new CreditCardTransactionFetcher(fetcherClient);
        CreditCardAccount account = getTestCreditCardAccount();
        // when

        PaginatorResponse firstPage = fetcher.getTransactionsFor(account, 1);
        PaginatorResponse secondPage = fetcher.getTransactionsFor(account, 2);
        // then

        assertThat(firstPage.getTinkTransactions()).hasSize(2);
        assertThat(secondPage.getTinkTransactions()).hasSize(1);

        Iterator<? extends Transaction> iterator = firstPage.getTinkTransactions().iterator();

        assertThatCardTransactionsAreMappedProperly(
                iterator.next(),
                LocalDate.of(2020, 7, 23),
                true,
                -4000.0,
                "Revolut**0873*",
                "003802");
        assertThatCardTransactionsAreMappedProperly(
                iterator.next(),
                LocalDate.of(2020, 7, 17),
                false,
                -597.05,
                "CRV*ST1 46101 ASKIM      London       GB",
                "2019901320011576");

        assertThatCardTransactionsAreMappedProperly(
                secondPage.getTinkTransactions().iterator().next(),
                LocalDate.of(2020, 7, 1),
                false,
                -173.5,
                "EUROSPAR RUGTVE          Stathelle    NO",
                "2018301320009896");
    }

    private CreditCardAccount getTestCreditCardAccount() {
        CreditCardAccount cardAccount = mock(CreditCardAccount.class);
        given(cardAccount.getApiIdentifier()).willReturn(CARD_ID);
        return cardAccount;
    }

    private void assertThatCardTransactionsAreMappedProperly(
            Transaction transaction,
            LocalDate expectedDate,
            boolean expectedPending,
            double expectedAmount,
            String expectedDescription,
            String expectedPayload) {

        assertThat(transaction.getType()).isEqualTo(TransactionTypes.CREDIT_CARD);
        assertThat(transaction.getDate())
                .isEqualTo(
                        new Date(
                                expectedDate
                                        .atTime(java.time.LocalTime.NOON)
                                        .atZone(ZoneId.of("CET"))
                                        .toInstant()
                                        .toEpochMilli()));
        assertThat(transaction.isPending()).isEqualTo(expectedPending);
        assertThat(transaction.getExactAmount())
                .isEqualTo(ExactCurrencyAmount.of(expectedAmount, "NOK"));
        assertThat(transaction.getDescription()).isEqualTo(expectedDescription);
        assertThat(transaction.getPayload()).hasSize(1);
        assertThat(transaction.getPayload().get(TransactionPayloadTypes.EXTERNAL_ID))
                .isEqualTo(expectedPayload);
    }
}
