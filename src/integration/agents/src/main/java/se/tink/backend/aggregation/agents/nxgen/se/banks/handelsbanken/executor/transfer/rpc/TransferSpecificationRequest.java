package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.TransferAmount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class TransferSpecificationRequest {

    private double amount;
    private String message;
    private String annotation;
    private TransferAmount fromAccount;
    private TransferAmount toAccount;
    private String transferDate;
    private String toClearingNo;

    private TransferSpecificationRequest() {}

    public static TransferSpecificationRequest create(
            Transfer transfer,
            AmountableSource source,
            AmountableDestination destination,
            TransferMessageFormatter.Messages messages) {
        TransferSpecificationRequest request = new TransferSpecificationRequest();
        request.transferDate = getCurrentDateAsString();
        request.amount = toAmount(transfer);
        request.message = messages.getDestinationMessage();
        request.annotation = messages.getSourceMessage();
        request.fromAccount = source.toTransferAmount();
        request.toAccount = destination.toTransferAmount();
        request.toClearingNo = ((SwedishIdentifier) transfer.getDestination()).getClearingNumber();
        return request;
    }

    private static double toAmount(Transfer transfer) {
        return new BigDecimal(transfer.getAmount().getValue())
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    private interface Amountable {
        TransferAmount toTransferAmount();
    }

    public interface AmountableSource extends Amountable {}

    public interface AmountableDestination extends Amountable {
        boolean isKnownDestination();
    }

    /**
     * This method returns the current date in correct time zone, as a string formatted as
     * yyyy-MM-dd
     */
    public static String getCurrentDateAsString() {
        Date date = new Date();
        LocalDateTime localDate = date.toInstant().atZone(ZoneId.of("CET")).toLocalDateTime();
        return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
