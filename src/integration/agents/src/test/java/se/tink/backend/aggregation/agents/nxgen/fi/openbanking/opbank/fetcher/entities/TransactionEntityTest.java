package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionEntityTest {

    private static final TransactionEntity TRANSACTION_RESPONSE_WITH_PAYER_PRESENT =
            SerializationUtils.deserializeFromString(
                    " { \"archiveId\" : \"ARCHIVE_ID\", \"message\": \"MESSAGE\", \"amount\": \"50.0\", \"currency\" : \"EUR\" , \"bookingDate\" : \"2000-10-10\", \"valueDate\" : \"2020-03-19\", \"payer\": { \"name\" : \"PAYER\" }, \"proprietaryTransactionDescription\" : \"CARD\" }",
                    TransactionEntity.class);
    private static final TransactionEntity TRANSACTION_RESPONSE_WITH_RECIPIENT_PRESENT =
            SerializationUtils.deserializeFromString(
                    " { \"archiveId\" : \"ARCHIVE_ID\", \"message\": \"MESSAGE\", \"amount\": \"50.0\", \"currency\" : \"EUR\" , \"bookingDate\" : \"2020-05-05\", \"valueDate\" : \"2020-03-19\", \"recipient\": { \"name\" : \"RECIPIENT\" }, \"proprietaryTransactionDescription\" : \"CARD\" }",
                    TransactionEntity.class);
    private static final TransactionEntity
            TRANSACTION_RESPONSE_WITHOUT_PAYER_AND_RECIPIENT_PRESENT =
                    SerializationUtils.deserializeFromString(
                            " { \"archiveId\" : \"ARCHIVE_ID\", \"message\": \"MESSAGE\", \"amount\": \"50.0\", \"currency\" : \"EUR\" , \"bookingDate\" : \"2020-05-05\", \"valueDate\" : \"2020-03-19\", \"proprietaryTransactionDescription\" : \"CARD\" }",
                            TransactionEntity.class);
    private static final TransactionEntity
            TRANSACTION_RESPONSE_WITHOUT_PAYER_AND_RECIPIENT_AND_MESSAGE_PRESENT =
                    SerializationUtils.deserializeFromString(
                            " { \"archiveId\" : \"ARCHIVE_ID\", \"amount\": \"50.0\", \"currency\" : \"EUR\" , \"bookingDate\" : \"2020-05-05\", \"valueDate\" : \"2020-03-19\", \"proprietaryTransactionDescription\" : \"CARD\" }",
                            TransactionEntity.class);
    private static final TransactionEntity TRANSACTION_RESPONSE_WITH_INVALID_DATE =
            SerializationUtils.deserializeFromString(
                    " { \"archiveId\" : \"ARCHIVE_ID\", \"message\": \"MESSAGE\", \"amount\": \"50.0\", \"currency\" : \"EUR\" , \"bookingDate\" : \"05-05-2020\", \"valueDate\" : \"2020-03-19\", \"proprietaryTransactionDescription\" : \"CARD\" }",
                    TransactionEntity.class);

    @Test
    public void shouldUsePayerAsDescriptionWhenPayerPresentInResponse() {
        Transaction transaction = TRANSACTION_RESPONSE_WITH_PAYER_PRESENT.toTinkTransaction();
        assertThat(transaction.getDescription()).isEqualTo("PAYER");
    }

    @Test
    public void shouldUseRecipientAsDescriptionWhenPayerNotPresentInResponse() {
        Transaction transaction = TRANSACTION_RESPONSE_WITH_RECIPIENT_PRESENT.toTinkTransaction();
        assertThat(transaction.getDescription()).isEqualTo("RECIPIENT");
    }

    @Test
    public void shouldUseMessageAsDescriptionWhenPayerAndRecipientNotPresentInResponse() {
        Transaction transaction =
                TRANSACTION_RESPONSE_WITHOUT_PAYER_AND_RECIPIENT_PRESENT.toTinkTransaction();
        assertThat(transaction.getDescription()).isEqualTo("MESSAGE");
    }

    @Test
    public void shouldSetEmptyDescriptionWhenAllEligibleFieldsAreMissing() {
        Transaction transaction =
                TRANSACTION_RESPONSE_WITHOUT_PAYER_AND_RECIPIENT_AND_MESSAGE_PRESENT
                        .toTinkTransaction();
        assertThat(transaction.getDescription()).isEqualTo("");
    }

    @Test
    public void shouldProperlyMapIntoTinkTransaction() {
        Transaction transaction = TRANSACTION_RESPONSE_WITH_PAYER_PRESENT.toTinkTransaction();
        assertThat(transaction.getDescription()).isEqualTo("PAYER");
        assertThat(transaction.getDate().toString()).isEqualTo("Tue Oct 10 10:00:00 UTC 2000");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(50.0));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenParsingErrorForBookingDate() {
        TRANSACTION_RESPONSE_WITH_INVALID_DATE.toTinkTransaction();
    }
}
