package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferPayloadType;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import se.tink.libraries.date.DateUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentEntity extends AbstractLinkResponse {
    private AccountEntity account;
    private AmountEntity amount;
    private String approvalId;
    private boolean changeAllowed;
    private boolean deleteAllowed;
    private String dueDate;
    private String message;
    private RecipientAccountEntity recipient;
    private DetailedPermissions detailedPermissions;
    private PaymentContextResponse context;

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public void setAmount(AmountEntity amount) {
        this.amount = amount;
    }

    public String getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public boolean isChangeAllowed() {
        return changeAllowed;
    }

    public void setChangeAllowed(boolean changeAllowed) {
        this.changeAllowed = changeAllowed;
    }

    public boolean isDeleteAllowed() {
        return deleteAllowed;
    }

    public void setDeleteAllowed(boolean deleteAllowed) {
        this.deleteAllowed = deleteAllowed;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RecipientAccountEntity getRecipient() {
        return recipient;
    }

    public void setRecipient(RecipientAccountEntity recipient) {
        this.recipient = recipient;
    }

    public Transfer toTransfer() {
        Transfer transfer = new Transfer();

        transfer.setDueDate(DateUtils.flattenTime(DateUtils.parseDate(dueDate)));
        transfer.setAmount(Amount.inSEK(StringUtils.parseAmount(amount.getAmountFormatted())));
        transfer.setDestination(recipient.generalGetAccountIdentifier());

        if (account != null) {
            SwedishSHBInternalIdentifier identifier = new SwedishSHBInternalIdentifier(account.getNumber());
            if (identifier.isValid()) {
                transfer.setSource(identifier);
            }
        }

        transfer.setDestinationMessage(getMessage());
        transfer.setSourceMessage(recipient.getName());

        if (approvalId != null) {
            transfer.addPayload(TransferPayloadType.PROVIDER_UNIQUE_ID, approvalId);
            transfer.setType(TransferType.EINVOICE);
        } else {
            transfer.setType(TransferType.PAYMENT);
        }

        return transfer;
    }

    public DetailedPermissions getDetailedPermissions() {
        return detailedPermissions;
    }

    public void setDetailedPermissions(DetailedPermissions detailedPermissions) {
        this.detailedPermissions = detailedPermissions;
    }

    public PaymentContextResponse getContext() {
        return context;
    }

    public void setContext(PaymentContextResponse context) {
        this.context = context;
    }
}
