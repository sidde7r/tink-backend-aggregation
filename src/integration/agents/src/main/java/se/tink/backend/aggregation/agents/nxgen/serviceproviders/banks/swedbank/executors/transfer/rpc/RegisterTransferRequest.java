package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc;

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
    private final String fromAccountId;
    private final String date;

    private RegisterTransferRequest(double amount, String recipientId, String fromAccountId) {
        this.amount = amount;
        this.recipientId = recipientId;
        this.fromAccountId = fromAccountId;
        this.date = EMPTY_DATE;
    }

    public static RegisterTransferRequest create(
            double amount, String recipientId, String fromAccountId) {
        return new RegisterTransferRequest(amount, recipientId, fromAccountId);
    }
}
