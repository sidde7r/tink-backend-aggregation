package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment.rpc;

import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.BaseSignRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.PaymentRecipient;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class PaymentSignRequest implements BaseSignRequest {
    @JsonDouble(outputType = JsonDouble.JsonType.STRING, trailingZeros = false)
    private double amount;

    private String payDate;
    private String recipientName;
    private String message;
    private String recipientId;
    private String accountNumber;
    private int recipientType;

    public static PaymentSignRequest create(Transfer transfer, PaymentRecipient paymentRecipient) {
        PaymentSignRequest request = new PaymentSignRequest();
        request.amount = transfer.getAmount().getValue();

        request.payDate =
                FORMATTER_DAILY.format(
                        transfer.getDueDate() != null
                                ? transfer.getDueDate()
                                : DateUtils.getNextBusinessDay(new Date()));

        request.recipientName = paymentRecipient.getName();
        request.message = transfer.getDestinationMessage();
        request.recipientId = paymentRecipient.getId();
        AccountIdentifier account = transfer.getSource();
        request.accountNumber =
                (account instanceof SwedishIdentifier
                        ? ((SwedishIdentifier) account).getAccountNumber()
                        : account.getIdentifier());
        request.recipientType = paymentRecipient.getType();
        return request;
    }
}
