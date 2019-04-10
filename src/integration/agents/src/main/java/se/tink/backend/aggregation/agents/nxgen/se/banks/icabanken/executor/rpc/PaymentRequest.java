package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenExecutorUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class PaymentRequest implements TransferRequest {
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

    private PaymentRequest(
            Transfer transfer, AccountEntity sourceAccount, RecipientEntity destinationAccount) {

        this.recipientAccountNumber = destinationAccount.getAccountNumber();
        this.amount = transfer.getAmount().getValue();
        this.registrationId = null;
        this.memo = transfer.getSourceMessage();
        this.fromAccountId = sourceAccount.getAccountId();
        this.reference = transfer.getDestinationMessage();
        this.type = destinationAccount.getType();
        this.isStandingTransaction = false;
        this.eventId = null;
        this.dueDate = IcaBankenExecutorUtils.findOrCreateDueDateFor(transfer);
        this.type = IcaBankenConstants.Transfers.PAYMENT;
        this.referenceType = IcaBankenExecutorUtils.getReferenceTypeFor(transfer);
        this.recipientId = destinationAccount.getRecipientId();
    }

    public static PaymentRequest create(
            Transfer transfer, AccountEntity sourceAccount, RecipientEntity destinationAccount) {

        return new PaymentRequest(transfer, sourceAccount, destinationAccount);
    }
}
