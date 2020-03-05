package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import static java.time.LocalDate.parse;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.TransactionsResponse.Response;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.TransactionsResponse.Response.ItemContainer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.TransactionsResponse.Response.ItemContainer.Item;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.TransactionsResponse.Response.ItemContainer.Item.AmountEntry;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionsMapperTest {

    private static final BigDecimal GIVEN_AMOUNT_1 = new BigDecimal("12");
    private static final BigDecimal GIVEN_AMOUNT_2 = new BigDecimal("34");
    private static final BigDecimal GIVEN_AMOUNT_3 = new BigDecimal("45");

    private static final LocalDate GIVEN_DATE_1 = parse("2020-02-01");
    private static final LocalDate GIVEN_DATE_2 = parse("2020-02-02");

    private static final String EXPECTED_DATE_1 = "2020-02-01T11:00:00.000>";
    private static final String EXPECTED_DATE_2 = "2020-02-02T11:00:00.000>";

    private static final String GIVEN_DESC_1 = "desc1";
    private static final String GIVEN_DESC_2 = "desc2";
    private static final String GIVEN_DESC_3 = "desc3";

    private static final String GIVEN_CURRENCY = "EUR";

    private TransactionsMapper tested = new TransactionsMapper();

    @Test
    public void toTransactionsShouldReturnValidObject() {
        // given
        TransactionsResponse givenTransactionsResponse = givenTransactionsResponse();

        // when
        List<Transaction> result = tested.toTransactions(givenTransactionsResponse);

        // then
        assertThat(result).hasSize(3);
        assertNotPendingTransaction(result.get(0), EXPECTED_DATE_1, GIVEN_DESC_1, GIVEN_AMOUNT_1);
        assertNotPendingTransaction(result.get(1), EXPECTED_DATE_2, GIVEN_DESC_2, GIVEN_AMOUNT_2);
        assertPendingTransaction(result.get(2), GIVEN_DESC_3, GIVEN_AMOUNT_3);
    }

    private TransactionsResponse givenTransactionsResponse() {
        return new TransactionsResponse()
                .setResponse(
                        new Response()
                                .setItemContainers(
                                        asList(
                                                givenItemContainer(
                                                        asList(
                                                                givenTransactionItem(
                                                                        GIVEN_DATE_1,
                                                                        GIVEN_DESC_1,
                                                                        GIVEN_AMOUNT_1),
                                                                givenTransactionItem(
                                                                        GIVEN_DATE_2,
                                                                        GIVEN_DESC_2,
                                                                        GIVEN_AMOUNT_2))),
                                                givenItemContainer(
                                                        singletonList(
                                                                givenTransactionItem(
                                                                        null,
                                                                        GIVEN_DESC_3,
                                                                        GIVEN_AMOUNT_3))))));
    }

    private ItemContainer givenItemContainer(List<Item> items) {
        return new ItemContainer().setItems(items);
    }

    private Item givenTransactionItem(LocalDate bookDate, String description, BigDecimal amount) {
        return new Item()
                .setBookDate(bookDate)
                .setDescription(description)
                .setAmountEntry(new AmountEntry().setAmount(amount).setCurrency(GIVEN_CURRENCY));
    }

    private void assertNotPendingTransaction(
            Transaction transaction,
            String expectedDate,
            String expectedDesc,
            BigDecimal expectedAmount) {
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.getDate()).isEqualTo(expectedDate);
        assertThat(transaction.getDescription()).isEqualTo(expectedDesc);

        assertThat(transaction.getExactAmount().getExactValue()).isEqualTo(expectedAmount);
        assertThat(transaction.getExactAmount().getCurrencyCode()).isEqualTo(GIVEN_CURRENCY);
    }

    private void assertPendingTransaction(
            Transaction transaction, String expectedDesc, BigDecimal expectedAmount) {
        assertThat(transaction.isPending()).isTrue();
        assertThat(transaction.getDescription()).isEqualTo(expectedDesc);

        assertThat(transaction.getExactAmount().getExactValue()).isEqualTo(expectedAmount);
        assertThat(transaction.getExactAmount().getCurrencyCode()).isEqualTo(GIVEN_CURRENCY);
    }
}
