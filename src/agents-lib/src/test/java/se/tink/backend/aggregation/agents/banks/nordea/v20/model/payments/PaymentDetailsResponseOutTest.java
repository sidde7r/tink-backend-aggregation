package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import static org.assertj.core.api.Assertions.assertThat;

public class PaymentDetailsResponseOutTest {
    private static final String RESPONSE_STRING = "{\"getPaymentDetailsOut\":{\"paymentId\": {\"$\": \"0987654321\"},\"paymentSubType\": {\"$\": \"eInvoice\"},\"paymentSubTypeExtension\": {\"$\": \"BGType\"},\"fromAccountId\": {\"$\": \"NDEASESSXXX-SE1-SEK-1122334455\"},\"toAccountId\": {},\"amount\": {\"$\": 133.39},\"currency\": {\"$\": \"SEK\"},\"dueDate\": {\"$\": \"2016-04-29T12:00:00.877+02:00\"},\"dueDateType\": {},\"paymentDate\": {\"$\": \"2016-04-29T12:00:00.877+02:00\"},\"beneficiaryName\": {\"$\": \"Recipient name\"},\"beneficiaryNickName\": {},\"reference\": {},\"messageRow\": {\"$\": \"123456\"},\"storePayment\": {},\"receiptCode\": {},\"recurringFrequency\": {\"$\": \"Once\"},\"recurringNumberOfPayments\": {\"$\": 0},\"statusCode\": {\"$\": \"Unconfirmed\"},\"timeStamp\": {},\"personalNote\": {},\"recurringContinuously\": {\"$\": false},\"category\": {},\"allowedToModify\": {\"$\": \"Yes\"},\"isAllowedToDelete\": {\"$\": true},\"isAllowedToCopy\": {\"$\": false},\"isAllowedToModifyFromAccountId\": {\"$\": true},\"isAllowedToModifyAmount\": {\"$\": true},\"isAllowedToModifyDueDate\": {\"$\": true},\"isAllowedToModifyStorePayment\": {\"$\": true},\"isAllowedToModifyPaymentSubTypeExtension\": {\"$\": false},\"isAllowedToModifyToAccount\": {\"$\": false},\"isAllowedToModifyMessage\": {\"$\": false},\"isAllowedToModifyRecurringfrequency\": {\"$\": false},\"isAllowedToModifyRecurringnumberOfPayments\": {\"$\": false},\"isAllowedToModifyRecurringContinuously\": {\"$\": false},\"isAllowedToModifyBeneficiaryName\": {\"$\": false},\"isAllowedToModifyBeneficiaryNickName\": {\"$\": false},\"statusExtensionCode\": {},\"beneficiaryBankId\": {},\"toAccountNumber\": {\"$\": \"11223344\"},\"eInvoiceToken\": {\"$\": \"asdfasdfasdfasdf\"}}}";

    @Test
    public void deserializesDifferentStrongTypes() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        PaymentDetailsResponse paymentDetailsResponse = objectMapper
                .readValue(RESPONSE_STRING, PaymentDetailsResponse.class);

        PaymentDetailsResponseOut paymentDetails = paymentDetailsResponse.getPaymentDetailsResponseOut();
        assertThat(paymentDetails).isNotNull();

        assertThat(paymentDetails.getPaymentId()).isEqualTo("0987654321");
        assertThat(paymentDetails.getDueDate().getTime()).isEqualTo(1461924000877L);
        assertThat(paymentDetails.getAmount()).isEqualTo(133.39);
        assertThat(paymentDetails.getPaymentSubType()).isEqualTo(Payment.SubType.EINVOICE);
        assertThat(paymentDetails.getPaymentSubTypeExtension().getType()).isEqualTo(AccountIdentifier.Type.SE_BG);
        assertThat(paymentDetails.getStatusCode()).isEqualTo(Payment.StatusCode.UNCONFIRMED);
        assertThat(paymentDetails.getToAccountId()).isEqualTo(null);
        assertThat(paymentDetails.isAllowedToModify()).isTrue();
        assertThat(paymentDetails.isAllowedToModifyAmount()).isTrue();
        assertThat(paymentDetails.isAllowedToModifyMessage()).isFalse();
    }

    @Test
    public void deserializeAndConvertToTransfer() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        PaymentDetailsResponse paymentDetailsResponse = objectMapper
                .readValue(RESPONSE_STRING, PaymentDetailsResponse.class);

        PaymentDetailsResponseOut paymentDetails = paymentDetailsResponse.getPaymentDetailsResponseOut();
        assertThat(paymentDetails).isNotNull();

        Transfer transfer = paymentDetails.toEInvoiceTransfer();
        assertThat(transfer.getType()).isEqualTo(TransferType.EINVOICE);
        assertThat(transfer.getDestination()).isEqualTo(new BankGiroIdentifier("11223344"));
        assertThat(transfer.getDestination().getName().orElse(null)).isEqualTo("Recipient name");
        assertThat(transfer.getDestinationMessage()).isEqualTo("123456");
        assertThat(transfer.getAmount()).isEqualTo(Amount.inSEK(133.39));
        assertThat(transfer.getSource()).isEqualTo(new SwedishIdentifier("1122334455"));
        assertThat(transfer.getSourceMessage()).isEqualTo("Recipient name");
        assertThat(transfer.getDueDate().getTime()).isEqualTo(1461924000000L);
        assertThat(transfer.getPayload()).isEmpty();
    }
}
