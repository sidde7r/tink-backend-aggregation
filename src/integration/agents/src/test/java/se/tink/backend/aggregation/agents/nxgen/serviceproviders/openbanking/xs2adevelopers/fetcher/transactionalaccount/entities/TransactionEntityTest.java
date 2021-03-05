package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class TransactionEntityTest {

    private static final String OUTGOING_WITH_CREDITOR_NAME =
            "{\"bookingDate\": \"2020-06-30\", \"creditorName\": \"Super Creditor Name\", \"remittanceInformationUnstructured\": \"RemittanceInformation 001\", \"transactionAmount\": {\"amount\": -999.0, \"currency\": \"EUR\"} }";
    private static final String OUTGOING_WITHOUT_CREDITOR_NAME =
            "{\"bookingDate\": \"2020-06-30\", \"remittanceInformationUnstructured\": \"RemittanceInformation 002\", \"transactionAmount\": {\"amount\": -999.0, \"currency\": \"EUR\"} }";
    private static final String OUTGOING_WITH_DEBTOR_NAME =
            "{\"bookingDate\": \"2020-06-30\", \"debtorName\": \"Super Debtor Name\", \"remittanceInformationUnstructured\": \"RemittanceInformation 003\", \"transactionAmount\": {\"amount\": 999.0, \"currency\": \"EUR\"} }";
    private static final String OUTGOING_WITHOUT_DEBTOR_NAME =
            "{\"bookingDate\": \"2020-06-30\", \"remittanceInformationUnstructured\": \"RemittanceInformation 004\", \"transactionAmount\": {\"amount\": 999.0, \"currency\": \"EUR\"} }";
    private static final String OUTGOING_WITH_PAYPAL_CREDITOR_NAME =
            "{\"bookingDate\": \"2020-06-30\", \"creditorName\": \"Paypal\", \"remittanceInformationUnstructured\": \"RemittanceInformation 005\", \"transactionAmount\": {\"amount\": -999.0, \"currency\": \"EUR\"} }";
    private static final String OUTGOING_WITH_BLANK_EVERYTHING =
            "{\"bookingDate\": \"2020-06-30\", \"creditorName\": \"\", \"remittanceInformationUnstructured\": \"\", \"transactionAmount\": {\"amount\": -999.0, \"currency\": \"EUR\"} }";

    @Test
    @Parameters(method = "allTransactionPossibilities")
    public void should_return_properly_mapped_transaction_description(
            String transactionJson, String expectedDescription, double expectedAmount) {
        // given
        TransactionEntity transactionEntity =
                SerializationUtils.deserializeFromString(transactionJson, TransactionEntity.class);
        // when
        Transaction transaction = transactionEntity.toBookedTinkTransaction();
        // then

        assertThat(transaction.getDescription()).isEqualTo(expectedDescription);
        assertThat(transaction.getExactAmount())
                .isEqualTo(ExactCurrencyAmount.of(expectedAmount, "EUR"));
    }

    @SuppressWarnings("unused")
    private Object[] allTransactionPossibilities() {
        return new Object[] {
            new Object[] {OUTGOING_WITH_CREDITOR_NAME, "Super Creditor Name", -999.0},
            new Object[] {OUTGOING_WITHOUT_CREDITOR_NAME, "RemittanceInformation 002", -999.0},
            new Object[] {OUTGOING_WITH_DEBTOR_NAME, "Super Debtor Name", 999.0},
            new Object[] {OUTGOING_WITHOUT_DEBTOR_NAME, "RemittanceInformation 004", 999.0},
            new Object[] {OUTGOING_WITH_PAYPAL_CREDITOR_NAME, "RemittanceInformation 005", -999.0},
            new Object[] {OUTGOING_WITH_BLANK_EVERYTHING, "", -999.0}
        };
    }
}
