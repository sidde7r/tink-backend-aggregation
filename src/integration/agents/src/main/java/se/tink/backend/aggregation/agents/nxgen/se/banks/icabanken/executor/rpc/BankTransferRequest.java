package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenExecutorUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class BankTransferRequest implements TransferRequest {
    @JsonProperty("RecipientAccountNumber")
    private String recipientAccountNumber;

    @JsonProperty("Amount")
    private double amount;

    @JsonProperty("RegistrationId")
    private String registrationId;

    @JsonProperty("Memo")
    private String memo;

    @JsonProperty("FromAccountId")
    private String fromAccountId;

    @JsonProperty("Reference")
    private String reference;

    @JsonProperty("RecipientType")
    private String recipientType;

    @JsonProperty("IsStandingTransaction")
    private boolean isStandingTransaction;

    @JsonProperty("EventId")
    private String eventId;

    @JsonProperty("DueDate")
    private String dueDate;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("ReferenceType")
    private String referenceType;

    @JsonProperty("RecipientId")
    private String recipientId;

    @Override
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    private BankTransferRequest(
            Transfer transfer,
            AccountEntity sourceAccount,
            RecipientEntity destinationAccount,
            TransferMessageFormatter transferMessageFormatter) {

        TransferMessageFormatter.Messages formattedMessages =
                transferMessageFormatter.getMessages(transfer, destinationAccount.isOwnAccount());

        this.recipientAccountNumber = destinationAccount.getAccountNumber();
        this.amount = transfer.getAmount().getValue();
        this.registrationId = null;
        this.memo = formattedMessages.getSourceMessage();
        this.fromAccountId = sourceAccount.getAccountId();
        this.reference = formattedMessages.getDestinationMessage();
        this.recipientType = IcaBankenConstants.Transfers.BANK_TRANSFER;
        this.isStandingTransaction = false;
        this.eventId = null;
        this.dueDate = IcaBankenExecutorUtils.findOrCreateDueDateFor(transfer);
        this.type = IcaBankenConstants.Transfers.BANK_TRANSFER;
        this.referenceType = null;
        this.recipientId = destinationAccount.getRecipientId();
    }

    public static BankTransferRequest create(
            Transfer transfer,
            AccountEntity sourceAccount,
            RecipientEntity destinationAccount,
            TransferMessageFormatter transferMessageFormatter) {

        return new BankTransferRequest(
                transfer, sourceAccount, destinationAccount, transferMessageFormatter);
    }
}
