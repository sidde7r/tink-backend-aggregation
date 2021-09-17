package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionEntityTest {

    private static final String BOOKED_TRX = "{\"Status\": \"Booked\"}";
    private static final String PENDING_TRX = "{\"Status\": \"Pending\"}";
    private static final String REJECTED_TRX = "{\"Status\": \"Rejected\"}";

    @Test
    public void settledTransactionIsNotRejected() {
        TransactionEntity entity =
                SerializationUtils.deserializeFromString(BOOKED_TRX, TransactionEntity.class);

        assertThat(entity.isNotRejected()).isTrue();
    }

    @Test
    public void pendingTransactionIsNotRejected() {
        TransactionEntity entity =
                SerializationUtils.deserializeFromString(PENDING_TRX, TransactionEntity.class);

        assertThat(entity.isNotRejected()).isTrue();
    }

    @Test
    public void rejectedTransactionIsRejected() {
        TransactionEntity entity =
                SerializationUtils.deserializeFromString(REJECTED_TRX, TransactionEntity.class);

        assertThat(entity.isNotRejected()).isFalse();
    }
}
