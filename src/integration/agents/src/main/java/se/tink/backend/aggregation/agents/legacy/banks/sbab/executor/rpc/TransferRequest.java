package se.tink.backend.aggregation.agents.banks.sbab.executor.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.banks.sbab.util.SBABDateUtil;
import se.tink.backend.aggregation.agents.banks.sbab.util.SBABDestinationAccountIdentifierFormatter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
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
    private TransferRequest(
            Transfer transfer,
            TransferMessageFormatter.Messages messages,
            boolean isInternalTransfer) {
        this.fromAccountNumber = transfer.getSource().getIdentifier();
        this.toAccountNumber =
                transfer.getDestination().getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER);
        this.amount = transfer.getAmount().getValue();
        this.transactionDate =
                SBABDateUtil.getTransferDate(transfer.getDueDate(), isInternalTransfer);
        this.recurringTransfer = false;
        this.noteToSender = messages.getSourceMessage();
        this.noteToRecipient = messages.getDestinationMessage();
        this.signatureProcessResponse = null;
    }

    @JsonIgnore
    public static TransferRequest create(
            Transfer transfer,
            TransferMessageFormatter.Messages messages,
            boolean isInternalTransfer) {
        return new TransferRequest(transfer, messages, isInternalTransfer);
    }

    public TransferRequest setSignatureProcessResponse(
            SignProcessResponse signatureProcessResponse) {
        this.signatureProcessResponse = signatureProcessResponse;
        return this;
    }
}
