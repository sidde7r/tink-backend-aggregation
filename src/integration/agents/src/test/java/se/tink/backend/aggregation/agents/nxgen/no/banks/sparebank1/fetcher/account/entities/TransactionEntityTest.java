package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionEntityTest {

    @Test
    public void ensureThatDescriptionIsFormattedCorrectly_case1() {
        TransactionEntity te = getTransactionEntityWithDescription("01.01 MERCHANT");
        assertThat(te.toTinkTransaction().getDescription()).isEqualTo("MERCHANT");
    }

    @Test
    public void ensureThatDescriptionIsFormattedCorrectly_case2() {
        TransactionEntity te = getTransactionEntityWithDescription("01.01 MERCHANT LOCATION");
        assertThat(te.toTinkTransaction().getDescription()).isEqualTo("MERCHANT LOCATION");
    }

    @Test
    public void ensureThatDescriptionIsFormattedCorrectly_case3() {
        TransactionEntity te =
                getTransactionEntityWithDescription("*1234 01.01 SEK 10.00 MERCHANT Kurs: 1.0000");
        assertThat(te.toTinkTransaction().getDescription()).isEqualTo("MERCHANT");
    }

    @Test
    public void ensureThatDescriptionIsFormattedCorrectly_case4() {
        TransactionEntity te =
                getTransactionEntityWithDescription(
                        "*1234 01.01 SEK 10.00 MERCHANT LOCATION Kurs: 1.0000");
        assertThat(te.toTinkTransaction().getDescription()).isEqualTo("MERCHANT LOCATION");
    }

    @Test
    public void ensureThatDescriptionIsFormattedCorrectly_case5() {
        TransactionEntity te =
                getTransactionEntityWithDescription(
                        "*1234 01.01 SEK 100.00 MERCHANT LOCATION Kurs: 1.0000");
        assertThat(te.toTinkTransaction().getDescription()).isEqualTo("MERCHANT LOCATION");
    }

    @Test
    public void ensureThatDescriptionIsFormattedCorrectly_case6() {
        TransactionEntity te =
                getTransactionEntityWithDescription("*1234 01.01 NOK 10.00 MERCHANT Kurs: 1.0000");
        assertThat(te.toTinkTransaction().getDescription()).isEqualTo("MERCHANT");
    }

    @Test
    public void ensureThatDescriptionIsFormattedCorrectly_case7() {
        TransactionEntity te =
                getTransactionEntityWithDescription("*1234 01.01 NOK 10.00 MERCHANT Kurs: 10.0000");
        assertThat(te.toTinkTransaction().getDescription()).isEqualTo("MERCHANT");
    }

    @Test
    public void ensureThatDescriptionIsFormattedCorrectly_case8() {
        TransactionEntity te = getTransactionEntityWithDescription("MERCHANT");
        assertThat(te.toTinkTransaction().getDescription()).isEqualTo("MERCHANT");
    }

    @Test
    public void ensureThatDescriptionIsFormattedCorrectly_case9() {
        TransactionEntity te = getTransactionEntityWithDescription("MERCHANT LOCATION");
        assertThat(te.toTinkTransaction().getDescription()).isEqualTo("MERCHANT LOCATION");
    }

    @Test
    public void ensureThatDescriptionIsFormattedCorrectly_case10() {
        TransactionEntity te = getTransactionEntityWithDescription("MERCHANT Kurs: 10.0000");
        assertThat(te.toTinkTransaction().getDescription()).isEqualTo("MERCHANT Kurs: 10.0000");
    }

    @Test
    public void ensurePendingIsSetToTrue_whenId_isNull() {
        TransactionEntity te = getTransactionEntity();
        Transaction transaction = te.toTinkTransaction();
        assertThat(transaction.isPending()).isEqualTo(true);
    }

    private TransactionEntity getTransactionEntity() {
        TransactionEntity te = new TransactionEntity();
        te.setDescription("MERCHANT");
        te.setAmount(new BigDecimal("10.25"));
        return te;
    }

    private TransactionEntity getTransactionEntityWithDescription(String description) {
        TransactionEntity te = new TransactionEntity();
        te.setDescription(description);
        te.setAmount(new BigDecimal("10.25"));
        return te;
    }
}
