package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.rpc;

import static se.tink.backend.aggregation.annotations.JsonDouble.JsonType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterTransferRequest {
    @JsonIgnore private static final String EMPTY_DATE = "";

    @JsonDouble(outputType = JsonType.STRING, decimalSeparator = ',')
    private final double amount;

    private final String recipientId;
    private final String noteToRecipient;
    private final String fromAccountId;
    private final String date;

    private RegisterTransferRequest(
            double amount, String recipientId, String noteToRecipient, String fromAccountId) {
        this.amount = amount;
        this.recipientId = recipientId;
        this.noteToRecipient = noteToRecipient;
        this.fromAccountId = fromAccountId;
        this.date = EMPTY_DATE;
    }

    public static RegisterTransferRequest create(
            double amount, String recipientId, String noteToRecipient, String fromAccountId) {
        return new RegisterTransferRequest(amount, recipientId, noteToRecipient, fromAccountId);
    }
}
