package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.rpc;

import static se.tink.backend.aggregation.annotations.JsonDouble.JsonType;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class RegisterTransferRequest {

    @JsonDouble(outputType = JsonType.STRING, decimalSeparator = ',')
    private final double amount;

    private final String recipientId;
    private final String noteToRecipient;
    private final String fromAccountId;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Stockholm")
    private final Date date;

    public static RegisterTransferRequest create(
            double amount,
            String recipientId,
            String noteToRecipient,
            String fromAccountId,
            Date transferDueDate) {
        return new RegisterTransferRequest(
                amount, recipientId, noteToRecipient, fromAccountId, transferDueDate);
    }
}
