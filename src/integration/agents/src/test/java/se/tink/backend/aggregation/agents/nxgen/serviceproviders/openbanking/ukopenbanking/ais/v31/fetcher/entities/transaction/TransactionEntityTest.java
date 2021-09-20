package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class TransactionEntityTest {

    private static final String BOOKED_TRX = "{\"Status\": \"Booked\"}";
    private static final String PENDING_TRX = "{\"Status\": \"Pending\"}";
    private static final String REJECTED_TRX = "{\"Status\": \"Rejected\"}";

    private static final String BOOKED_WITH_SUPPLEMENTARY_DATA_TRX =
            "{\"Status\": \"Booked\", \"SupplementaryData\": { }}";
    private static final String PENDING_WITH_SUPPLEMENTARY_DATA_TRX =
            "{\"Status\": \"Pending\", \"SupplementaryData\": { }}";
    private static final String REJECTED_WITH_SUPPLEMENTARY_DATA_TRX =
            "{\"Status\": \"Rejected\", \"SupplementaryData\": { }}";

    private static final String PENDING_DECLINED_TRX =
            "{\"Status\": \"Pending\", \"SupplementaryData\": { \"Declined\": true }}";
    private static final String BOOKED_DECLINED_TRX =
            "{\"Status\": \"Booked\", \"SupplementaryData\": { \"Declined\": true }}";
    private static final String BOOKED_NOT_DECLINED_TRX =
            "{\"Status\": \"Booked\", \"SupplementaryData\": { \"Declined\": false }}";

    @Test
    @Parameters(method = "notRejectedDummyTransactions")
    public void isNotRejected(TransactionEntity transaction) {
        assertThat(transaction.isNotRejected()).isTrue();
    }

    @Test
    @Parameters(method = "rejectedDummyTransactions")
    public void isRejected(TransactionEntity transaction) {
        assertThat(transaction.isNotRejected()).isFalse();
    }

    @Test
    @Parameters(method = "notDeclinedDummyTransactions")
    public void isNotDeclined(TransactionEntity transaction) {
        assertThat(transaction.isNotDeclined()).isTrue();
    }

    @Test
    @Parameters(method = "declinedDummyTransactions")
    public void isDeclined(TransactionEntity transaction) {
        assertThat(transaction.isNotDeclined()).isFalse();
    }

    @SuppressWarnings("unused")
    private Object[] rejectedDummyTransactions() {
        return new Object[] {
            new Object[] {
                SerializationUtils.deserializeFromString(REJECTED_TRX, TransactionEntity.class)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        REJECTED_WITH_SUPPLEMENTARY_DATA_TRX, TransactionEntity.class)
            }
        };
    }

    @SuppressWarnings("unused")
    private Object[] notRejectedDummyTransactions() {
        return new Object[] {
            new Object[] {
                SerializationUtils.deserializeFromString(BOOKED_TRX, TransactionEntity.class)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(PENDING_TRX, TransactionEntity.class)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        BOOKED_WITH_SUPPLEMENTARY_DATA_TRX, TransactionEntity.class)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        PENDING_WITH_SUPPLEMENTARY_DATA_TRX, TransactionEntity.class)
            }
        };
    }

    @SuppressWarnings("unused")
    private Object[] notDeclinedDummyTransactions() {
        return new Object[] {
            new Object[] {
                SerializationUtils.deserializeFromString(BOOKED_TRX, TransactionEntity.class)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(PENDING_TRX, TransactionEntity.class)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        BOOKED_WITH_SUPPLEMENTARY_DATA_TRX, TransactionEntity.class)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        PENDING_WITH_SUPPLEMENTARY_DATA_TRX, TransactionEntity.class)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        BOOKED_NOT_DECLINED_TRX, TransactionEntity.class)
            }
        };
    }

    @SuppressWarnings("unused")
    private Object[] declinedDummyTransactions() {
        return new Object[] {
            new Object[] {
                SerializationUtils.deserializeFromString(
                        PENDING_DECLINED_TRX, TransactionEntity.class)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        BOOKED_DECLINED_TRX, TransactionEntity.class)
            }
        };
    }
}
