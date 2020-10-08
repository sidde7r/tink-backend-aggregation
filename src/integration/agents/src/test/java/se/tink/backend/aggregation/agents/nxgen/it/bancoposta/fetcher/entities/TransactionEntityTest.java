package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionEntityTest {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Test
    public void toTinkTransactionShouldConvertTransactionEntityToTransaction() {
        // given
        TransactionEntity transactionEntity =
                TransactionEntity.builder()
                        .amountSymbol("+")
                        .amount("100")
                        .bookedDate(LocalDate.parse("16/02/2020", DATE_TIME_FORMATTER))
                        .shortDescription("dummyVal")
                        .valueDate(LocalDate.parse("14/02/2020", DATE_TIME_FORMATTER))
                        .build();
        // when
        Transaction transaction = transactionEntity.toTinkTransaction("dummyVal");
        // then
        assertThat(transaction.isPending()).isEqualTo(false);
        Date bookedDate =
                Date.from(LocalDateTime.of(2020, 02, 16, 11, 0, 0, 0).toInstant(ZoneOffset.UTC));
        assertThat(transaction.getDate()).isEqualTo(bookedDate);
        assertThat(transaction.getExactAmount())
                .isEqualTo(
                        ExactCurrencyAmount.of(new BigDecimal("100").movePointLeft(2), "dummyVal"));
    }

    @Test
    public void toTinkTransactionShouldSetPendingIfBookedDateNotAvailable() {
        // given
        TransactionEntity transactionEntity =
                TransactionEntity.builder()
                        .amountSymbol("-")
                        .amount("100")
                        .bookedDate(null)
                        .shortDescription("dummyVal")
                        .valueDate(LocalDate.parse("14/02/2020", DATE_TIME_FORMATTER))
                        .build();

        // when
        Transaction transaction = transactionEntity.toTinkTransaction("dummyVal");

        // then
        assertThat(transaction.isPending()).isEqualTo(true);
        Date pendingDate =
                Date.from(LocalDateTime.of(2020, 02, 14, 11, 0, 0, 0).toInstant(ZoneOffset.UTC));
        assertThat(transaction.getDate()).isEqualTo(pendingDate);
        assertThat(transaction.getExactAmount())
                .isEqualTo(
                        ExactCurrencyAmount.of(
                                new BigDecimal("-100").movePointLeft(2), "dummyVal"));
    }
}
