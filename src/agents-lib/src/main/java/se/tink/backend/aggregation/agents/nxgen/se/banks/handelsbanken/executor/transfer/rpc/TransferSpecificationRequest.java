package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.TransferAmount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class TransferSpecificationRequest {

    private double amount;
    private String message;
    private String annotation;
    private TransferAmount fromAccount;
    private TransferAmount toAccount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date transferDate;
    private String toClearingNo;

    private TransferSpecificationRequest() {
    }

    public static TransferSpecificationRequest create(Transfer transfer, AmountableSource source,
            AmountableDestination destination, TransferMessageFormatter.Messages messages) {
        TransferSpecificationRequest request = new TransferSpecificationRequest();
        request.transferDate = new Date();
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

    public interface AmountableSource extends Amountable {
    }

    public interface AmountableDestination extends Amountable {
        boolean isKnownDestination();
    }
}
