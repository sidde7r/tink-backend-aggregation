package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenExecutorUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferRequest {
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

    private TransferRequest(
            Transfer transfer,
            AccountEntity sourceAccount,
            RecipientEntity destinationAccount,
            String sourceMessage,
            String remittanceInformationValue,
            String typeOfPayment) {
        this.recipientAccountNumber = destinationAccount.getAccountNumber();
        this.amount = transfer.getAmount().getValue();
        this.registrationId = null;
        this.memo = sourceMessage;
        this.fromAccountId = sourceAccount.getAccountId();
        this.reference = remittanceInformationValue;
        this.type = destinationAccount.getType();
        this.isStandingTransaction = false;
        this.eventId = null;
        this.type = typeOfPayment;
        this.dueDate = IcaBankenExecutorUtils.getDueDate(transfer);
        if (IcaBankenConstants.Transfers.BANK_TRANSFER.equalsIgnoreCase(typeOfPayment)) {
            this.referenceType = null;
            this.recipientType = typeOfPayment;
        } else {
            this.referenceType = IcaBankenExecutorUtils.getReferenceTypeFor(transfer);
        }

        this.recipientId = destinationAccount.getRecipientId();
    }

    public static TransferRequest createPaymentRequest(
            Transfer transfer, AccountEntity sourceAccount, RecipientEntity destinationAccount) {
        return new TransferRequest(
                transfer,
                sourceAccount,
                destinationAccount,
                transfer.getSourceMessage(),
                transfer.getRemittanceInformation().getValue(),
                IcaBankenConstants.Transfers.PAYMENT);
    }

    public static TransferRequest createTransferRequest(
            Transfer transfer,
            AccountEntity sourceAccount,
            RecipientEntity destinationAccount,
            TransferMessageFormatter transferMessageFormatter) {
        TransferMessageFormatter.Messages formattedMessages =
                transferMessageFormatter.getMessagesFromRemittanceInformation(
                        transfer, destinationAccount.isOwnAccount());
        return new TransferRequest(
                transfer,
                sourceAccount,
                destinationAccount,
                formattedMessages.getSourceMessage(),
                formattedMessages.getDestinationMessage(),
                IcaBankenConstants.Transfers.BANK_TRANSFER);
    }
}
