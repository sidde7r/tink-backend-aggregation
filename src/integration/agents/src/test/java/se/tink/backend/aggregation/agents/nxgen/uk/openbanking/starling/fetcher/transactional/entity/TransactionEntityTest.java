package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.fetcher.transactional.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.TransactionEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionEntityTest {

    private static final String UPCOMING_TRX = "{\"status\": \"UPCOMING\"}";
    private static final String REVERSED_TRX = "{\"status\": \"REVERSED\"}";
    private static final String DECLINED_TRX = "{\"status\": \"DECLINED\"}";
    private static final String REFUNDED_TRX = "{\"status\": \"REFUNDED\"}";
    private static final String ACCOUNT_CHECK_TRX = "{\"status\": \"ACCOUNT_CHECK\"}";
    private static final String SETTLED_TRX = "{\"status\": \"SETTLED\"}";
    private static final String PENDING_TRX = "{\"status\": \"PENDING\"}";
    private static final String RETRYING_TRX = "{\"status\": \"RETRYING\"}";

    @Test
    public void upcomingTransactionIsIrrelevant() {
        TransactionEntity entity =
                SerializationUtils.deserializeFromString(UPCOMING_TRX, TransactionEntity.class);

        assertThat(entity.isRelevant()).isFalse();
    }

    @Test
    public void reversedTransactionIsIrrelevant() {
        TransactionEntity entity =
                SerializationUtils.deserializeFromString(REVERSED_TRX, TransactionEntity.class);

        assertThat(entity.isRelevant()).isFalse();
    }

    @Test
    public void declinedTransactionIsIrrelevant() {
        TransactionEntity entity =
                SerializationUtils.deserializeFromString(DECLINED_TRX, TransactionEntity.class);

        assertThat(entity.isRelevant()).isFalse();
    }

    @Test
    public void refundedTransactionIsIrrelevant() {
        TransactionEntity entity =
                SerializationUtils.deserializeFromString(REFUNDED_TRX, TransactionEntity.class);

        assertThat(entity.isRelevant()).isFalse();
    }

    @Test
    public void accountCheckTransactionIsIrrelevant() {
        TransactionEntity entity =
                SerializationUtils.deserializeFromString(
                        ACCOUNT_CHECK_TRX, TransactionEntity.class);

        assertThat(entity.isRelevant()).isFalse();
    }

    @Test
    public void settledTransactionRelevant() {
        TransactionEntity entity =
                SerializationUtils.deserializeFromString(SETTLED_TRX, TransactionEntity.class);

        assertThat(entity.isRelevant()).isTrue();
    }

    @Test
    public void settledTransactionIsRelevant() {
        TransactionEntity entity =
                SerializationUtils.deserializeFromString(PENDING_TRX, TransactionEntity.class);

        assertThat(entity.isRelevant()).isTrue();
    }

    @Test
    public void retryingTransactionIsRelevant() {
        TransactionEntity entity =
                SerializationUtils.deserializeFromString(RETRYING_TRX, TransactionEntity.class);

        assertThat(entity.isRelevant()).isTrue();
    }
}
