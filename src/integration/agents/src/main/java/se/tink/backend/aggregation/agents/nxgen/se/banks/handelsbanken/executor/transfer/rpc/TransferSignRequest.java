package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.BaseSignRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.TransferAmount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.utilities.HandelsbankenDateUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class TransferSignRequest implements BaseSignRequest {

    private double amount;
    private String message;
    private String annotation;
    private TransferAmount fromAccount;
    private TransferAmount toAccount;
    private String transferDate;
    private String toClearingNo;

    private TransferSignRequest() {}

    public static TransferSignRequest create(
            Transfer transfer,
            AmountableSource source,
            AmountableDestination destination,
            TransferMessageFormatter.Messages messages,
            boolean isInternalTransfer) {
        TransferSignRequest request = new TransferSignRequest();
        request.transferDate = getDueDate(transfer, isInternalTransfer);
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

    private static String getDueDate(Transfer transfer, boolean isInternalTransfer) {
        HandelsbankenDateUtils handelsbankenDateUtils = new HandelsbankenDateUtils();
        String formattedDate;

        if (isInternalTransfer) {
            formattedDate =
                    handelsbankenDateUtils.getTransferDateForInternalTransfer(
                            transfer.getDueDate());
        } else {
            formattedDate =
                    handelsbankenDateUtils.getTransferDateForExternalTransfer(
                            transfer.getDueDate());
        }
        return formattedDate;
    }
}
