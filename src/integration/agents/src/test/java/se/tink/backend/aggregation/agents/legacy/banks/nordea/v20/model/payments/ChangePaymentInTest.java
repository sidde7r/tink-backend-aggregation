package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.text.ParseException;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.ProductEntity;
import se.tink.libraries.date.ThreadSafeDateFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangePaymentInTest {
    private static final String GET_PAYMENT_DETAILS_OUT = "{\"paymentId\":{\"$\":\"asdfasdfasdfasdf\"},\"paymentSubType\":{\"$\":\"eInvoice\"},\"paymentSubTypeExtension\":{\"$\":\"BGType\"},\"fromAccountId\":{\"$\":\"NDEASESSXXX-SE1-SEK-8607015537\"},\"toAccountId\":{},\"amount\":{\"$\":123.45},\"currency\":{\"$\":\"SEK\"},\"dueDate\":{\"$\":\"2016-04-30T12:00:00.454+02:00\"},\"dueDateType\":{},\"paymentDate\":{\"$\":\"2016-04-30T12:00:00.454+02:00\"},\"beneficiaryName\":{\"$\":\"messagemessage\"},\"beneficiaryNickName\":{},\"reference\":{},\"messageRow\":{\"$\":\"1212121212\"},\"storePayment\":{},\"receiptCode\":{},\"recurringFrequency\":{\"$\":\"Once\"},\"recurringNumberOfPayments\":{\"$\":0},\"statusCode\":{\"$\":\"Unconfirmed\"},\"timeStamp\":{},\"personalNote\":{},\"recurringContinuously\":{\"$\":false},\"category\":{},\"allowedToModify\":{\"$\":\"Yes\"},\"isAllowedToDelete\":{\"$\":true},\"isAllowedToCopy\":{\"$\":false},\"isAllowedToModifyFromAccountId\":{\"$\":true},\"isAllowedToModifyAmount\":{\"$\":true},\"isAllowedToModifyDueDate\":{\"$\":true},\"isAllowedToModifyStorePayment\":{\"$\":true},\"isAllowedToModifyPaymentSubTypeExtension\":{\"$\":false},\"isAllowedToModifyToAccount\":{\"$\":false},\"isAllowedToModifyMessage\":{\"$\":false},\"isAllowedToModifyRecurringfrequency\":{\"$\":false},\"isAllowedToModifyRecurringnumberOfPayments\":{\"$\":false},\"isAllowedToModifyRecurringContinuously\":{\"$\":false},\"isAllowedToModifyBeneficiaryName\":{\"$\":false},\"isAllowedToModifyBeneficiaryNickName\":{\"$\":false},\"statusExtensionCode\":{},\"beneficiaryBankId\":{},\"toAccountNumber\":{\"$\":\"12345678\"},\"eInvoiceToken\":{\"$\":\"sometoken\"}}";

    @Test
    public void copyFromPaymentDetails() throws IOException, ParseException {
        // This actually tests also deserialization of PaymentDetailsResponseOut, but was convenient just to get some test fast of this copy method
        ObjectMapper objectMapper = new ObjectMapper();
        PaymentDetailsResponseOut paymentDetails = objectMapper
                .readValue(GET_PAYMENT_DETAILS_OUT, PaymentDetailsResponseOut.class);

        ChangePaymentRequest changePaymentRequest = ChangePaymentRequest.copyFromPaymentDetails(mockFromAccounts(), paymentDetails);

        // Test just a few values for now to check that we wire values
        assertThat(changePaymentRequest.getChangePaymentIn()).isNotNull();
        assertThat(changePaymentRequest.getChangePaymentIn().getAmount()).isEqualTo(paymentDetails.getAmount());
        assertThat(changePaymentRequest.getChangePaymentIn().getPaymentSubType()).isEqualTo(Payment.SubType.NORMAL);
        assertThat(changePaymentRequest.getChangePaymentIn().getFromAccountId()).isEqualTo("abc123-abc123");
        assertThat(changePaymentRequest.getChangePaymentIn().getDueDate())
                .isBetween(ThreadSafeDateFormat.FORMATTER_SECONDS.parse("2016-04-30 00:00:00"), ThreadSafeDateFormat.FORMATTER_SECONDS.parse("2016-04-30 23:59:59"));
    }

    @Test
    public void serializingDoesNotThrowAndContainsSomeExpectedValue() throws IOException, ParseException {
        ChangePaymentIn changePaymentIn = new ChangePaymentIn();
        changePaymentIn.setAddBeneficiary(false);
        changePaymentIn.setAmount(123.45);
        changePaymentIn.setBeneficiaryName("benname");
        changePaymentIn.setBeneficiaryNickName("nickname");
        changePaymentIn.setCurrency("currency");
        changePaymentIn.setDueDate(ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-05-12"));
        changePaymentIn.setDueDateTypeDueDatePayment();
        changePaymentIn.setFromAccountId("abc123-abc123");
        changePaymentIn.setMessageRow("message");
        changePaymentIn.setPaymentSubType(Payment.SubType.NORMAL);
        changePaymentIn.setPaymentSubTypeExtension(Payment.SubTypeExtension.SE_BG);
        changePaymentIn.setReceiptCodeNoReceipt();
        changePaymentIn.setRecurringContinuously(false);
        changePaymentIn.setRecurringFrequencyOnce();
        changePaymentIn.setRecurringNumberOfPayments(123);
        changePaymentIn.setStatusCode(Payment.StatusCode.UNCONFIRMED);
        changePaymentIn.setStorePayment(false);
        changePaymentIn.setToAccountId("XXXToAccountId");

        ObjectMapper objectMapper = new ObjectMapper();
        String serialized = objectMapper.writeValueAsString(changePaymentIn);

        assertThat(serialized).contains("\"XXXToAccountId\"");
        assertThat(serialized).contains("\"Unconfirmed\"");
        assertThat(serialized).contains("\"2015-05-12\"");
        assertThat(serialized).contains("\"123.45\"");
        assertThat(serialized).contains("\"No\"");
        assertThat(serialized).contains("\"false\"");
        assertThat(serialized).contains("\"Normal\"");
        assertThat(serialized).contains("\"BGType\"");
        assertThat(serialized).contains("\"abc123-abc123\"");
    }

    private ImmutableList<ProductEntity> mockFromAccounts() {
        ProductEntity fromAccount = mock(ProductEntity.class);
        when(fromAccount.getAccountId()).thenReturn("NDEASESSXXX-SE1-SEK-8607015537");
        when(fromAccount.getInternalId()).thenReturn("abc123-abc123");
        return ImmutableList.of(fromAccount);
    }
}