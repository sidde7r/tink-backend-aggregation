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
    private String amount;

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

    public String getMemo() {
        return memo;
    }

    private TransferRequest(
            Transfer transfer,
            AccountEntity sourceAccount,
            RecipientEntity destinationAccount,
            String sourceMessage,
            String remittanceInformationValue,
            String typeOfPayment) {
        this.recipientAccountNumber = destinationAccount.getAccountNumber();
        this.amount = String.valueOf(transfer.getAmount().getValue());
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
            this.recipientType = destinationAccount.getType();

            // TODO: these 3 lines + method should be removed after customers start using only RI
            if (transfer.getRemittanceInformation().getType() == null) {
                IcaBankenExecutorUtils.validateAndSetRemittanceInformationType(transfer);
            }
            switch (transfer.getRemittanceInformation().getType()) {
                case OCR:
                    this.referenceType = IcaBankenConstants.Transfers.OCR;
                    break;
                case UNSTRUCTURED:
                    this.referenceType = IcaBankenConstants.Transfers.MESSAGE;
                    break;
                default:
                    throw new IllegalStateException("Unknown remittance information type");
            }
        }
        this.recipientId = destinationAccount.getRecipientId();
    }

    public static TransferRequest createPaymentRequest(
            Transfer transfer, AccountEntity sourceAccount, RecipientEntity destinationAccount) {
        return new TransferRequest(
                transfer,
                sourceAccount,
                destinationAccount,
                IcaBankenExecutorUtils.getTruncatedSourceMessage(transfer),
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
