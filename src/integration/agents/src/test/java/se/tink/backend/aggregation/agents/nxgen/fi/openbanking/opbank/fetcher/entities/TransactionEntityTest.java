package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionEntityTest {

    private static final TransactionEntity TRANSACTION_RESPONSE_WITH_CREDITOR_PRESENT =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "            \"transactionId\": \"dummyTransactionId\",\n"
                            + "            \"accountId\": \"dummyAccountId\",\n"
                            + "            \"archiveId\": \"dummyArchiveId\",\n"
                            + "            \"reference\": \"111111\",\n"
                            + "            \"message\": \"dummyMessage\",\n"
                            + "            \"amount\": \"100.0\",\n"
                            + "            \"currency\": \"EUR\",\n"
                            + "            \"creditDebitIndicator\": \"debit\",\n"
                            + "            \"accountBalance\": \"145.45\",\n"
                            + "            \"creditor\": {\n"
                            + "                \"accountIdentifierType\": \"IBAN\",\n"
                            + "                \"accountName\": \"CREDITOR\",\n"
                            + "                \"accountIdentifier\": \"id1\",\n"
                            + "                \"servicerIdentifier\": \"ser_id1\",\n"
                            + "                \"servicerIdentifierType\": \"BIC\"\n"
                            + "            },\n"
                            + "            \"debtor\": {\n"
                            + "                \"accountIdentifierType\": \"IBAN\",\n"
                            + "                \"accountName\": \"DEBTOR\",\n"
                            + "                \"accountIdentifier\": \"id2\",\n"
                            + "                \"servicerIdentifier\": \"ser_id2\",\n"
                            + "                \"servicerIdentifierType\": \"BIC\"\n"
                            + "            },\n"
                            + "            \"bookingDateTime\": \"2017-04-05T10:43:07T\",\n"
                            + "            \"valueDateTime\": \"2017-04-05T10:43:07\",\n"
                            + "            \"status\": \"Authorized\",\n"
                            + "            \"isoTransactionCode\": \"string\",\n"
                            + "            \"opTransactionCode\": \"string\",\n"
                            + "            \"_links\": {\n"
                            + "                \"account\": {\n"
                            + "                    \"href\": \"accHref\"\n"
                            + "                }\n"
                            + "            }\n"
                            + "        }",
                    TransactionEntity.class);
    private static final TransactionEntity TRANSACTION_RESPONSE_WITH_DEBTOR_PRESENT =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "            \"transactionId\": \"dummyTransactionId\",\n"
                            + "            \"accountId\": \"dummyAccountId\",\n"
                            + "            \"archiveId\": \"dummyArchiveId\",\n"
                            + "            \"reference\": \"111111\",\n"
                            + "            \"message\": \"dummyMessage\",\n"
                            + "            \"amount\": \"100.0\",\n"
                            + "            \"currency\": \"EUR\",\n"
                            + "            \"creditDebitIndicator\": \"debit\",\n"
                            + "            \"accountBalance\": \"145.45\",\n"
                            + "            \"debtor\": {\n"
                            + "                \"accountIdentifierType\": \"IBAN\",\n"
                            + "                \"accountName\": \"DEBTOR\",\n"
                            + "                \"accountIdentifier\": \"id2\",\n"
                            + "                \"servicerIdentifier\": \"ser_id2\",\n"
                            + "                \"servicerIdentifierType\": \"BIC\"\n"
                            + "            },\n"
                            + "            \"bookingDateTime\": \"2017-04-05T10:43:07T\",\n"
                            + "            \"valueDateTime\": \"2017-04-05T10:43:07T\",\n"
                            + "            \"status\": \"Authorized\",\n"
                            + "            \"isoTransactionCode\": \"string\",\n"
                            + "            \"opTransactionCode\": \"string\",\n"
                            + "            \"_links\": {\n"
                            + "                \"account\": {\n"
                            + "                    \"href\": \"accHref\"\n"
                            + "                }\n"
                            + "            }\n"
                            + "        }",
                    TransactionEntity.class);
    private static final TransactionEntity
            TRANSACTION_RESPONSE_WITHOUT_CREDITOR_AND_DEBTOR_PRESENT =
                    SerializationUtils.deserializeFromString(
                            "        {\n"
                                    + "            \"transactionId\": \"dummyTransactionId\",\n"
                                    + "            \"accountId\": \"dummyAccountId\",\n"
                                    + "            \"archiveId\": \"dummyArchiveId\",\n"
                                    + "            \"reference\": \"111111\",\n"
                                    + "            \"message\": \"MESSAGE\",\n"
                                    + "            \"amount\": \"100.0\",\n"
                                    + "            \"currency\": \"EUR\",\n"
                                    + "            \"creditDebitIndicator\": \"debit\",\n"
                                    + "            \"accountBalance\": \"145.45\",\n"
                                    + "            \"bookingDateTime\": \"2017-04-05T10:43:07T\",\n"
                                    + "            \"valueDateTime\": \"2017-04-05T10:43:07T\",\n"
                                    + "            \"status\": \"Authorized\",\n"
                                    + "            \"isoTransactionCode\": \"string\",\n"
                                    + "            \"opTransactionCode\": \"string\",\n"
                                    + "            \"_links\": {\n"
                                    + "                \"account\": {\n"
                                    + "                    \"href\": \"accHref\"\n"
                                    + "                }\n"
                                    + "            }\n"
                                    + "        }",
                            TransactionEntity.class);
    private static final TransactionEntity
            TRANSACTION_RESPONSE_WITHOUT_CREDITOR_AND_DEBTOR_AND_MESSAGE_PRESENT =
                    SerializationUtils.deserializeFromString(
                            "        {\n"
                                    + "            \"transactionId\": \"dummyTransactionId\",\n"
                                    + "            \"accountId\": \"dummyAccountId\",\n"
                                    + "            \"archiveId\": \"dummyArchiveId\",\n"
                                    + "            \"reference\": \"111111\",\n"
                                    + "            \"amount\": \"100.0\",\n"
                                    + "            \"currency\": \"EUR\",\n"
                                    + "            \"creditDebitIndicator\": \"debit\",\n"
                                    + "            \"accountBalance\": \"145.45\",\n"
                                    + "            \"bookingDateTime\": \"2017-04-05T10:43:07T\",\n"
                                    + "            \"valueDateTime\": \"2017-04-05T10:43:07T\",\n"
                                    + "            \"status\": \"Authorized\",\n"
                                    + "            \"isoTransactionCode\": \"string\",\n"
                                    + "            \"opTransactionCode\": \"string\",\n"
                                    + "            \"_links\": {\n"
                                    + "                \"account\": {\n"
                                    + "                    \"href\": \"accHref\"\n"
                                    + "                }\n"
                                    + "            }\n"
                                    + "        }",
                            TransactionEntity.class);
    private static final TransactionEntity TRANSACTION_RESPONSE_WITH_INVALID_DATE =
            SerializationUtils.deserializeFromString(
                    "        {\n"
                            + "            \"transactionId\": \"dummyTransactionId\",\n"
                            + "            \"accountId\": \"dummyAccountId\",\n"
                            + "            \"archiveId\": \"dummyArchiveId\",\n"
                            + "            \"reference\": \"111111\",\n"
                            + "            \"message\": \"MESSAGE\",\n"
                            + "            \"amount\": \"100.0\",\n"
                            + "            \"currency\": \"EUR\",\n"
                            + "            \"creditDebitIndicator\": \"debit\",\n"
                            + "            \"accountBalance\": \"145.45\",\n"
                            + "            \"bookingDateTime\": \"2017-04-05T\",\n"
                            + "            \"valueDateTime\": \"2017-04-05T\",\n"
                            + "            \"status\": \"Authorized\",\n"
                            + "            \"isoTransactionCode\": \"string\",\n"
                            + "            \"opTransactionCode\": \"string\",\n"
                            + "            \"_links\": {\n"
                            + "                \"account\": {\n"
                            + "                    \"href\": \"accHref\"\n"
                            + "                }\n"
                            + "            }\n"
                            + "        }",
                    TransactionEntity.class);

    @Test
    public void shouldUseCreditorAsDescriptionWhenCreditorPresentInResponse() {
        Transaction transaction = TRANSACTION_RESPONSE_WITH_CREDITOR_PRESENT.toTinkTransaction();
        assertThat(transaction.getDescription()).isEqualTo("CREDITOR");
    }

    @Test
    public void shouldUseRecipientAsDescriptionWhenPayerNotPresentInResponse() {
        Transaction transaction = TRANSACTION_RESPONSE_WITH_DEBTOR_PRESENT.toTinkTransaction();
        assertThat(transaction.getDescription()).isEqualTo("DEBTOR");
    }

    @Test
    public void shouldUseMessageAsDescriptionWhenPayerAndRecipientNotPresentInResponse() {
        Transaction transaction =
                TRANSACTION_RESPONSE_WITHOUT_CREDITOR_AND_DEBTOR_PRESENT.toTinkTransaction();
        assertThat(transaction.getDescription()).isEqualTo("MESSAGE");
    }

    @Test
    public void shouldSetEmptyDescriptionWhenAllEligibleFieldsAreMissing() {
        Transaction transaction =
                TRANSACTION_RESPONSE_WITHOUT_CREDITOR_AND_DEBTOR_AND_MESSAGE_PRESENT
                        .toTinkTransaction();
        assertThat(transaction.getDescription()).isEqualTo("");
    }

    @Test
    public void shouldProperlyMapIntoTinkTransaction() {
        Transaction transaction = TRANSACTION_RESPONSE_WITH_CREDITOR_PRESENT.toTinkTransaction();
        assertThat(transaction.getDescription()).isEqualTo("CREDITOR");
        assertThat(transaction.getDate().toString()).isEqualTo("Wed Apr 05 10:00:00 UTC 2017");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(100.0));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenParsingErrorForBookingDate() {
        TRANSACTION_RESPONSE_WITH_INVALID_DATE.toTinkTransaction();
    }
}
