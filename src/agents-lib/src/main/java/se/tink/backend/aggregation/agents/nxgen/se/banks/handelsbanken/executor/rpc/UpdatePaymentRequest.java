package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc;

import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class UpdatePaymentRequest {
    private String accountNumber;
    @JsonDouble(outputType = JsonDouble.JsonType.STRING, trailingZeros = false)
    private double amount;
    private String message;
    private String payDate;

    public static UpdatePaymentRequest create(Transfer transfer) {
        UpdatePaymentRequest request = new UpdatePaymentRequest();
        AccountIdentifier account = transfer.getSource();
        request.accountNumber = (account instanceof SwedishIdentifier
                ? ((SwedishIdentifier) account).getAccountNumber()
                : account.getIdentifier());
        request.amount = transfer.getAmount().getValue();
        request.message = transfer.getDestinationMessage();
        request.payDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(transfer.getDueDate());
        return request;
    }
}
