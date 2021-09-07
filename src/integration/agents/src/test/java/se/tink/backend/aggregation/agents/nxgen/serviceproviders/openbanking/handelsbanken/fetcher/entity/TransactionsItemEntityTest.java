package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionsItemEntityTest {

    @Test
    public void hasDateShouldReturnTrueIfAnyDateIsPresent() {
        // given
        TransactionsItemEntity transactionsItemsEntityWithOnlyLedgerDate =
                getTransactionsItemsEntityWithOnlyLedgerDate();
        TransactionsItemEntity transactionsItemsEntityWithOnlyTransactionDate =
                getTransactionsItemsEntityWithOnlyTransactionDate();
        TransactionsItemEntity transactionsItemsEntityWithOnlyValueDate =
                getTransactionsItemsEntityWithOnlyValueDate();

        // when
        Boolean hasDateWhenOnlyLedgerDatePresent =
                transactionsItemsEntityWithOnlyLedgerDate.hasDate();
        Boolean hasDateWhenOnlyTransactionDatePresent =
                transactionsItemsEntityWithOnlyTransactionDate.hasDate();
        Boolean hasDateWhenOnlyValueDatePresent =
                transactionsItemsEntityWithOnlyValueDate.hasDate();

        // then
        assertThat(hasDateWhenOnlyLedgerDatePresent).isTrue();
        assertThat(hasDateWhenOnlyTransactionDatePresent).isTrue();
        assertThat(hasDateWhenOnlyValueDatePresent).isTrue();
    }

    @Test
    public void hasDateShoulReturnFalseIfNoDatesPresent() {
        // given
        TransactionsItemEntity transactionsItemsEntityWithNoDate =
                getTransactionsItemsEntityWithNoDate();

        // when
        Boolean hasDate = transactionsItemsEntityWithNoDate.hasDate();

        // then
        assertThat(hasDate).isFalse();
    }

    private TransactionsItemEntity getTransactionsItemsEntityWithOnlyLedgerDate() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"amount\": {\n"
                        + "    \"content\": 100,\n"
                        + "    \"currency\": \"SEK\"\n"
                        + "  },\n"
                        + "  \"creditDebit\": \"DEBITED\",\n"
                        + "  \"ledgerDate\": \"2020-11-28\",\n"
                        + "  \"remittanceInformation\": \"Dummy remittanceInformation\",\n"
                        + "  \"status\": \"BOOKED\"\n"
                        + "}",
                TransactionsItemEntity.class);
    }

    private TransactionsItemEntity getTransactionsItemsEntityWithOnlyTransactionDate() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"amount\": {\n"
                        + "    \"content\": 100,\n"
                        + "    \"currency\": \"SEK\"\n"
                        + "  },\n"
                        + "  \"creditDebit\": \"DEBITED\",\n"
                        + "  \"transactionDate\": \"2020-11-28\",\n"
                        + "  \"remittanceInformation\": \"Dummy remittanceInformation\",\n"
                        + "  \"status\": \"BOOKED\"\n"
                        + "}",
                TransactionsItemEntity.class);
    }

    private TransactionsItemEntity getTransactionsItemsEntityWithOnlyValueDate() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"amount\": {\n"
                        + "    \"content\": 100,\n"
                        + "    \"currency\": \"SEK\"\n"
                        + "  },\n"
                        + "  \"creditDebit\": \"DEBITED\",\n"
                        + "  \"valueDate\": \"2020-11-28\",\n"
                        + "  \"remittanceInformation\": \"Dummy remittanceInformation\",\n"
                        + "  \"status\": \"BOOKED\"\n"
                        + "}",
                TransactionsItemEntity.class);
    }

    private TransactionsItemEntity getTransactionsItemsEntityWithNoDate() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"amount\": {\n"
                        + "    \"content\": 100,\n"
                        + "    \"currency\": \"SEK\"\n"
                        + "  },\n"
                        + "  \"creditDebit\": \"DEBITED\",\n"
                        + "  \"remittanceInformation\": \"Dummy remittanceInformation\",\n"
                        + "  \"status\": \"BOOKED\"\n"
                        + "}",
                TransactionsItemEntity.class);
    }
}
