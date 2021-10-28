package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.utils.SkandiaBankenExecutorUtils.formatGiroNumber;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities.PaymentSourceAccount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AddRecipientRequest {
    private String encryptedFromBankAccountNumber;
    private String recipientNumber;

    private AddRecipientRequest(Transfer transfer, PaymentSourceAccount sourceAccount) {
        this.encryptedFromBankAccountNumber = sourceAccount.getEncryptedBankAccountNumber();
        this.recipientNumber = formatGiroNumber(transfer);
    }

    public static AddRecipientRequest createAddRecipientRequest(
            Transfer transfer, PaymentSourceAccount sourceAccount) {
        return new AddRecipientRequest(transfer, sourceAccount);
    }
}
