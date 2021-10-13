package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities.PaymentSourceAccount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.enums.AccountIdentifierType;
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

    @JsonIgnore private int offset;

    private PaymentRequest(Transfer transfer, PaymentSourceAccount sourceAccount) {
        this.amount = transfer.getAmount().toBigDecimal();

        this.date =
                new ThreadSafeDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                        .format(transfer.getDueDate());

        this.encryptedFromBankAccountNumber = sourceAccount.getEncryptedBankAccountNumber();

        this.fromBankAccountTransactionNote = transfer.getSourceMessage();

        if (transfer.getDestination().getType().equals(AccountIdentifierType.SE_BG)) {
            offset = 3;
        } else if (transfer.getDestination().getType().equals(AccountIdentifierType.SE_PG)) {
            offset = 4;
        }
        this.giroNumber =
                new StringBuilder(transfer.getDestination().getIdentifier())
                        .insert(offset, "-")
                        .toString();

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
