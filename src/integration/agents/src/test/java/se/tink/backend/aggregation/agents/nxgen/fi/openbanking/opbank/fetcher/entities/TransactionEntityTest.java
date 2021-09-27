package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionEntityTest {

    private static final String TRANSACTION_RESPONSE_WITH_PAYER_AS_STRING =
            "{\"archiveId\":\"ARCHIVE_ID\",\"message\":\"MESSAGE\",\"reference\":\"00000000003591297866\",\"amount\":\"50.0\",\"currency\":\"EUR\",\"bookingDate\":\"2000-10-10\",\"valueDate\":\"2020-03-19\",\"paymentDate\":null,\"recipient\":null,\"proprietaryTransactionDescription\":\"CARD\",\"payer\":{\"name\":\"PAYER\"}}";

    private static final TransactionEntity TRANSACTION_RESPONSE_WITH_PAYER_PRESENT =
            SerializationUtils.deserializeFromString(
                    TRANSACTION_RESPONSE_WITH_PAYER_AS_STRING, TransactionEntity.class);
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
            TRANSACTION_RESPONSE_WITHOUT_PAYER_AND_RECIPIENT_AND_MESSAGE_AND_PROPRIETARY_TRANSACTION_DESCRIPTION =
                    SerializationUtils.deserializeFromString(
                            " { \"archiveId\" : \"ARCHIVE_ID\", \"amount\": \"50.0\", \"currency\" : \"EUR\" , \"bookingDate\" : \"2020-05-05\", \"valueDate\" : \"2020-03-19\", \"proprietaryTransactionDescription\" : \"\" }",
                            TransactionEntity.class);
    private static final TransactionEntity
            TRANSACTION_RESPONSE_WITHOUT_PAYER_AND_RECIPIENT_AND_MESSAGE_AND_PROPRIETARY_TRANSACTION_DESCRIPTION_PRESENT =
                    SerializationUtils.deserializeFromString(
                            "{\"archiveId\": \"ARCHIVE_ID\", \"message\": \"\", \"amount\": \"-50.00\", \"currency\": \"EUR\", \"bookingDate\": \"2020-10-09\", \"valueDate\": \"2020-10-09\", \"paymentDate\": \"2020-10-09\", \"recipient\": {\"name\": \"\", \"address\": []}, \"proprietaryTransactionDescription\": \"PTD\"}",
                            TransactionEntity.class);
    private static final TransactionEntity TRANSACTION_RESPONSE_WITH_INVALID_DATE =
            SerializationUtils.deserializeFromString(
                    " { \"archiveId\" : \"ARCHIVE_ID\", \"message\": \"MESSAGE\", \"amount\": \"50.0\", \"currency\" : \"EUR\" , \"bookingDate\" : \"05-05-2020\", \"valueDate\" : \"2020-03-19\", \"proprietaryTransactionDescription\" : \"CARD\" }",
                    TransactionEntity.class);

    @Test
    public void shouldUsePayerAsDescriptionWhenPayerPresentInResponse() {
        // given & when
        Transaction transaction = TRANSACTION_RESPONSE_WITH_PAYER_PRESENT.toTinkTransaction();

        // then
        assertThat(transaction.getDescription()).isEqualTo("PAYER");
    }

    @Test
    public void shouldUseRecipientAsDescriptionWhenPayerNotPresentInResponse() {
        // given & when
        Transaction transaction = TRANSACTION_RESPONSE_WITH_RECIPIENT_PRESENT.toTinkTransaction();

        // then
        assertThat(transaction.getDescription()).isEqualTo("RECIPIENT");
    }

    @Test
    public void shouldUseMessageAsDescriptionWhenPayerAndRecipientNotPresentInResponse() {
        // given & when
        Transaction transaction =
                TRANSACTION_RESPONSE_WITHOUT_PAYER_AND_RECIPIENT_PRESENT.toTinkTransaction();

        // then
        assertThat(transaction.getDescription()).isEqualTo("MESSAGE");
    }

    @Test
    public void
            shouldUseProprietaryTransactionDescriptionAsDescriptionWhenPayerAndRecipientNotPresentInResponse() {
        // given & when
        Transaction transaction =
                TRANSACTION_RESPONSE_WITHOUT_PAYER_AND_RECIPIENT_AND_MESSAGE_AND_PROPRIETARY_TRANSACTION_DESCRIPTION_PRESENT
                        .toTinkTransaction();

        // then
        assertThat(transaction.getDescription()).isEqualTo("PTD");
    }

    @Test
    public void shouldSetEmptyDescriptionWhenAllEligibleFieldsAreMissing() {
        // given & when
        Transaction transaction =
                TRANSACTION_RESPONSE_WITHOUT_PAYER_AND_RECIPIENT_AND_MESSAGE_AND_PROPRIETARY_TRANSACTION_DESCRIPTION
                        .toTinkTransaction();

        // then
        assertThat(transaction.getDescription()).isEmpty();
    }

    @Test
    public void shouldProperlyMapIntoTinkTransaction() {
        // given & when
        Transaction transaction = TRANSACTION_RESPONSE_WITH_PAYER_PRESENT.toTinkTransaction();

        // then
        assertThat(transaction.getDate().toString()).isEqualTo("Tue Oct 10 10:00:00 UTC 2000");
        assertThat(transaction.getDescription()).isEqualTo("PAYER");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(50.0));
        assertThat(transaction.getExternalSystemIds())
                .isEqualTo(
                        Collections.singletonMap(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                "ARCHIVE_ID"));
        assertThat(transaction.getTransactionDates())
                .usingRecursiveComparison()
                .isEqualTo(
                        TransactionDates.builder()
                                .setBookingDate(
                                        new AvailableDateInformation(LocalDate.parse("2000-10-10")))
                                .setValueDate(
                                        new AvailableDateInformation(LocalDate.parse("2020-03-19")))
                                .build());
        assertThat(transaction.getTransactionReference()).isEqualTo("00000000003591297866");
        assertThat(transaction.getRawDetails())
                .isEqualTo(TRANSACTION_RESPONSE_WITH_PAYER_AS_STRING);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenParsingErrorForBookingDate() {
        TRANSACTION_RESPONSE_WITH_INVALID_DATE.toTinkTransaction();
    }
}
