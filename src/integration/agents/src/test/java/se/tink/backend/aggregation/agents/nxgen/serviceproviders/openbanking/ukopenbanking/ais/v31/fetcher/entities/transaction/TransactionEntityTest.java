package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

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

    @Test
    @Parameters(method = "getParameters")
    public void shouldGetDescriptionWithProvidedEndToEndId(String endToEnd, String expectedResult) {
        // given
        TransactionEntity transactionEntity =
                getTransactionEntityWithTransactionInformationEndToEnd(endToEnd);

        // when
        String result = transactionEntity.getDescription();

        // then
        assertEquals(expectedResult, result);
    }

    @Test
    public void shouldGetDescriptionWithNotProvidedEndToEndId() {
        // given
        String information = "Some description";
        TransactionEntity transactionEntity =
                getTransactionEntityWithTransactionInformationWithoutEndToEnd(information);

        // when
        String result = transactionEntity.getDescription();

        // then
        assertEquals(information, result);
    }

    @Test
    public void shouldGetDescriptionFromTransactionReference() {
        // given
        TransactionEntity transactionEntity = getTransactionEntityWithoutTransactionInformation();
        String trxReference = transactionEntity.getTransactionReference();

        // when
        String result = transactionEntity.getDescription();

        // then
        assertEquals(trxReference, result);
    }

    private TransactionEntity getTransactionEntityWithTransactionInformationEndToEnd(
            String information) {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"AccountId\": \"c21a68b3-17ce-4e5c-acb6-9ec411331467\",\n"
                        + "    \"TransactionId\": \"ef3e6307-b67d-4a01-b66d-0a6370738819\",\n"
                        + "    \"TransactionReference\": \"980156\",\n"
                        + "    \"TransactionInformation\": \"ÖVERFÖRING\\nEndToEndID: "
                        + information
                        + "\"\n"
                        + "}",
                TransactionEntity.class);
    }

    private TransactionEntity getTransactionEntityWithTransactionInformationWithoutEndToEnd(
            String information) {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"AccountId\": \"c21a68b3-17ce-4e5c-acb6-9ec411331467\",\n"
                        + "    \"TransactionId\": \"ef3e6307-b67d-4a01-b66d-0a6370738819\",\n"
                        + "    \"TransactionReference\": \"980156\",\n"
                        + "    \"TransactionInformation\": \""
                        + information
                        + "\"\n"
                        + "}",
                TransactionEntity.class);
    }

    private TransactionEntity getTransactionEntityWithoutTransactionInformation() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"AccountId\": \"c21a68b3-17ce-4e5c-acb6-9ec411331467\",\n"
                        + "    \"TransactionId\": \"ef3e6307-b67d-4a01-b66d-0a6370738819\",\n"
                        + "    \"TransactionReference\": \"980156\"\n"
                        + "}",
                TransactionEntity.class);
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

    private Object[] getParameters() {
        return new Object[] {
            new Object[] {"NOTPROVIDED", "ÖVERFÖRING"},
            new Object[] {"NOT PROVIDED", "ÖVERFÖRING"},
            new Object[] {"Some description", "ÖVERFÖRING\n: Some description"},
        };
    }
}
