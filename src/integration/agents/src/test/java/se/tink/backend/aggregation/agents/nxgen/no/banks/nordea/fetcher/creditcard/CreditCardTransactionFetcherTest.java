package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;
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

    private static final String FIRST_PAGE_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/resources/creditCardTransactionsPage1.json";

    private static final String SECOND_PAGE_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/resources/creditCardTransactionsPage2.json";

    private static final CreditCardTransactionsResponse FIRST_PAGE_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(FIRST_PAGE_FILE_PATH), CreditCardTransactionsResponse.class);
    private static final CreditCardTransactionsResponse SECOND_PAGE_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(SECOND_PAGE_FILE_PATH), CreditCardTransactionsResponse.class);

    private static final String CARD_ID = "1234567";

    @Test
    public void shouldReturnProperlyMappedCreditCardTransactions() {
        // given
        FetcherClient fetcherClient = mock(FetcherClient.class);
        given(fetcherClient.fetchCreditCardTransactions(CARD_ID, 1))
                .willReturn(FIRST_PAGE_RESPONSE);
        given(fetcherClient.fetchCreditCardTransactions(CARD_ID, 2))
                .willReturn(SECOND_PAGE_RESPONSE);
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
