package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.junit.Test;

public class TransactionsWrapperEntityTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void getBookedWhenTransactionListIsEmpty() {
        // given
        TransactionsWrapperEntity entity = new TransactionsWrapperEntity();

        // when
        List<TransactionEntity> result = entity.getBooked();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getBookedWhenTransactionListIsNotEmpty() {
        // given
        TransactionsWrapperEntity entity =
                transactionsAsJson(
                        Arrays.asList(transactionEntityProps("1"), transactionEntityProps("2")));
        // when
        List<TransactionEntity> result = entity.getBooked();

        // then
        for (TransactionEntity transaction : result) {
            assertThat(transaction.getTransactionId())
                    .isIn("test-transaction-id-1", "test-transaction-id-2");
        }
    }

    private Properties transactionEntityProps(final String suffix) {
        Properties transaction = new Properties();
        transaction.setProperty("transactionId", "test-transaction-id-" + suffix);
        return transaction;
    }

    private static TransactionsWrapperEntity transactionsAsJson(
            final Collection<Properties> transactions) {
        Gson gsonObj = new Gson();
        try {
            return OBJECT_MAPPER.readValue(
                    "{\"booked\":" + gsonObj.toJson(transactions) + "}",
                    TransactionsWrapperEntity.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
