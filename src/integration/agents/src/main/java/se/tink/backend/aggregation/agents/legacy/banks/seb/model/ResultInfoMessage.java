package se.tink.backend.aggregation.agents.banks.seb.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.Builder;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonObject
public class ResultInfoMessage {

    public String TableName;
    public Integer ErrorRowId;
    public String ErrorColumnName;
    public String Level;
    public String ErrorCode;
    public String ErrorText;

    public String getErrorCode() {
        return ErrorCode != null ? ErrorCode.trim() : null;
    }

    public String getErrorText() {
        String text = ErrorText != null ? ErrorText.trim() : null;
        return "2000".equalsIgnoreCase(getErrorCode()) ? "Banken har blockerat överföringen" : text;
    }

    public void abortTransferAndThrow() throws TransferExecutionException {
        if (!Strings.isNullOrEmpty(getErrorCode())) {
            switch (getErrorCode()) {
                case "PCB046H":
                case "PCB046N":
                case "PCB049H":
                case "PCB049I":
                    throw cancelTransfer(
                            EndUserMessage.INVALID_DUEDATE_TOO_SOON_OR_NOT_BUSINESSDAY,
                            InternalStatus.INVALID_DUE_DATE);
                case "PCB046Q":
                    throw cancelTransfer(
                            EndUserMessage.DUPLICATE_PAYMENT, InternalStatus.DUPLICATE_PAYMENT);
                case "PCB03H6":
                    throw cancelTransfer(
                            EndUserMessage.INVALID_DESTINATION_MESSAGE,
                            InternalStatus.INVALID_DESTINATION_MESSAGE_TYPE);
                case "PCB03G0":
                    throw cancelTransfer(
                            EndUserMessage.EXCESS_AMOUNT, InternalStatus.INSUFFICIENT_FUNDS);
                case "PCB03K1":
                case "PCB03L1":
                case "PCB0354":
                    throw cancelTransfer(
                            EndUserMessage.INVALID_DESTINATION,
                            InternalStatus.INVALID_DESTINATION_ACCOUNT);
                case "PCB03E3":
                    throw cancelTransfer(
                            EndUserMessage.EXISTING_UNSIGNED_TRANSFERS,
                            InternalStatus.EXISTING_UNSIGNED_TRANSFERS);
                case "PCB0464":
                    throw cancelTransfer(null, InternalStatus.USER_REQUIRES_TRANSFER_PERMISSION);
                case "PCB0792":
                    throw cancelTransfer(
                            EndUserMessage.EXCESS_AMOUNT_FOR_BENEFICIARY,
                            InternalStatus.TRANSFER_LIMIT_REACHED);
                case "2000":
                    throw cancelTransfer(null, InternalStatus.ACCOUNT_BLOCKED_FOR_TRANSFER);
                default:
                    // NOP
            }
        }
        throw failTransfer(
                String.format(
                        "Unknown error: %s",
                        MoreObjects.toStringHelper(this)
                                .add("ErrorText", getErrorText())
                                .add("ErrorCode", getErrorCode())
                                .toString()));
    }

    private TransferExecutionException failTransfer(String message) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(getErrorText())
                .setMessage(message)
                .build();
    }

    private TransferExecutionException cancelTransfer(
            EndUserMessage endUserMessage, InternalStatus internalStatus) {
        Builder builder =
                TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setInternalStatus(internalStatus.toString());
        if (endUserMessage != null) {
            builder.setEndUserMessage(endUserMessage);
            builder.setMessage(endUserMessage.getKey().get());
        } else {
            builder.setEndUserMessage(getErrorText());
            builder.setMessage(getErrorText());
        }

        return builder.build();
    }
}
