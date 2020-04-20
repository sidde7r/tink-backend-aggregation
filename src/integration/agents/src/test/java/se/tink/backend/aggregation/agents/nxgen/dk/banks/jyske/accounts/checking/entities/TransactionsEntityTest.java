package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionsEntityTest {

    private static final double TRANSACTION_MAIN_AMOUNT = 345.56;
    private static final String TRANSACTION_TEXT = "sample transaction entity text";
    private static final String TRANSACTION_DATE = "20.03.2020";

    private static final String TRANSACTION_ENTITY_JSON =
            "{\n"
                    + "    \"mainAmount\": "
                    + TRANSACTION_MAIN_AMOUNT
                    + ","
                    + "    \"text\": \""
                    + TRANSACTION_TEXT
                    + "\","
                    + "    \"transactionDate\": \""
                    + TRANSACTION_DATE
                    + "\""
                    + "}";

    private static final String TRANSACTION_ENTITY_NO_DATE_JSON =
            "{\n"
                    + "    \"mainAmount\": "
                    + TRANSACTION_MAIN_AMOUNT
                    + ","
                    + "    \"text\": \""
                    + TRANSACTION_TEXT
                    + "\""
                    + "}";

    private TransactionsEntity transactionsEntity;

    @Test
    public void toTinkTransactionShouldReturnTransaction() throws IOException {
        // given
        transactionsEntity =
                new ObjectMapper().readValue(TRANSACTION_ENTITY_JSON, TransactionsEntity.class);

        // when
        Transaction result = transactionsEntity.toTinkTransaction();

        // then
        assertThat(result.getDescription()).isEqualTo(TRANSACTION_TEXT);
        assertThat(result.getExactAmount())
                .isEqualTo(ExactCurrencyAmount.of(TRANSACTION_MAIN_AMOUNT, "DKK"));
    }

    @Test
    public void toTinkTransactionShouldThrowExceptionWhenDateIsUnparsable() throws IOException {
        // given
        transactionsEntity =
                new ObjectMapper()
                        .readValue(TRANSACTION_ENTITY_NO_DATE_JSON, TransactionsEntity.class);

        // when
        Throwable t = Assertions.catchThrowable(() -> transactionsEntity.toTinkTransaction());

        // then
        assertThat(t)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("could not parse date: null (with pattern: dd.MM.yyyy)");
    }

    @Test
    public void toTinkUpcomingTransactionShouldReturnTransaction() throws IOException {
        // given
        transactionsEntity =
                new ObjectMapper().readValue(TRANSACTION_ENTITY_JSON, TransactionsEntity.class);

        // when
        UpcomingTransaction result = transactionsEntity.toTinkUpcomingTransaction();

        // then
        assertThat(result.getDescription()).isEqualTo(TRANSACTION_TEXT);
        assertThat(result.getExactAmount())
                .isEqualTo(ExactCurrencyAmount.of(TRANSACTION_MAIN_AMOUNT, "DKK"));
    }

    @Test
    public void toTinkUpcomingTransactionShouldThrowExceptionWhenDateIsUnparsable()
            throws IOException {
        // given
        transactionsEntity =
                new ObjectMapper()
                        .readValue(TRANSACTION_ENTITY_NO_DATE_JSON, TransactionsEntity.class);

        // when
        Throwable t =
                Assertions.catchThrowable(() -> transactionsEntity.toTinkUpcomingTransaction());

        // then
        assertThat(t)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("could not parse date: null (with pattern: dd.MM.yyyy)");
    }
}
