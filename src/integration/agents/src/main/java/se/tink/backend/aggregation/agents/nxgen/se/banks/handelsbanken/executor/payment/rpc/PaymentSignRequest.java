package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment.rpc;

import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.BaseSignRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.PaymentRecipient;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.date.CountryDateHelper;
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

    public static PaymentSignRequest create(
            Transfer transfer, PaymentRecipient paymentRecipient, String firstPayDate) {
        PaymentSignRequest request = new PaymentSignRequest();
        request.amount = transfer.getAmount().getValue();

        if (Objects.nonNull(transfer.getDueDate())) {
            request.payDate = FORMATTER_DAILY.format(transfer.getDueDate());
        } else if (Objects.nonNull(firstPayDate)) {
            request.payDate = firstPayDate;
        } else {
            final CountryDateHelper helper = new CountryDateHelper(new Locale("sv", "SE"));
            request.payDate = FORMATTER_DAILY.format(helper.getNextBusinessDay(new Date()));
        }

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
