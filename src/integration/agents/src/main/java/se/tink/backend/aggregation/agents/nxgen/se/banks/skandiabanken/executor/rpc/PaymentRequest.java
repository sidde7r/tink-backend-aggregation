package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.SkandiaBankenExecutorUtils.formatGiroNumber;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities.PaymentSourceAccount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.Transfer;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class PaymentRequest {

    private BigDecimal amount;
    private String date;
    private String encryptedFromBankAccountNumber;
    private String fromBankAccountTransactionNote;
    private String giroNumber;
    private String messageReference;
    private String ocrReference;

    private PaymentRequest(Transfer transfer, PaymentSourceAccount sourceAccount) {
        this.amount = transfer.getAmount().toBigDecimal();

        this.date =
                ThreadSafeDateFormat.FORMATTER_MILLISECONDS_WITH_TIMEZONE.format(
                        transfer.getDueDate());

        this.encryptedFromBankAccountNumber = sourceAccount.getEncryptedBankAccountNumber();

        this.fromBankAccountTransactionNote = transfer.getSourceMessage();

        this.giroNumber = formatGiroNumber(transfer);

        if (transfer.getRemittanceInformation().isOfType(RemittanceInformationType.OCR)) {
            this.ocrReference = transfer.getRemittanceInformation().getValue();
        } else if (transfer.getRemittanceInformation()
                .isOfType(RemittanceInformationType.UNSTRUCTURED)) {
            this.messageReference = transfer.getRemittanceInformation().getValue();
        }
    }

    public static PaymentRequest createPaymentRequest(
            Transfer transfer, PaymentSourceAccount sourceAccount) {
        return new PaymentRequest(transfer, sourceAccount);
    }
}
