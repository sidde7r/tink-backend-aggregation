package se.tink.backend.aggregation.agents.banks.sbab.executor.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.agents.banks.sbab.util.SBABDestinationAccountIdentifierFormatter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class TransferRequest {

    @JsonIgnore
    private static final SBABDestinationAccountIdentifierFormatter ACCOUNT_IDENTIFIER_FORMATTER =
            new SBABDestinationAccountIdentifierFormatter();

    private String fromAccountNumber;
    private String toAccountNumber;
    private double amount;
    private String transactionDate;
    private boolean recurringTransfer;
    private String noteToSender;
    private String noteToRecipient;
    private SignProcessResponse signatureProcessResponse;

    @JsonIgnore
    private TransferRequest(Transfer transfer, TransferMessageFormatter.Messages messages) {
        this.fromAccountNumber = transfer.getSource().getIdentifier();
        this.toAccountNumber =
                transfer.getDestination().getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER);
        this.amount = transfer.getAmount().getValue();
        this.transactionDate = formatDueDateIfPresent(transfer.getDueDate());
        this.recurringTransfer = false;
        this.noteToSender = messages.getSourceMessage();
        this.noteToRecipient = messages.getDestinationMessage();
        this.signatureProcessResponse = null;
    }

    @JsonIgnore
    public static TransferRequest create(
            Transfer transfer, TransferMessageFormatter.Messages messages) {
        return new TransferRequest(transfer, messages);
    }

    @JsonIgnore
    private String formatDueDateIfPresent(Date dueDate) {

        if (dueDate == null) {
            return null;
        }

        return ThreadSafeDateFormat.FORMATTER_DAILY.format(dueDate);
    }

    public TransferRequest setSignatureProcessResponse(
            SignProcessResponse signatureProcessResponse) {
        this.signatureProcessResponse = signatureProcessResponse;
        return this;
    }
}
