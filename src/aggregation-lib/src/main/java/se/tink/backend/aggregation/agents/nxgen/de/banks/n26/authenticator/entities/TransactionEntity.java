package se.tink.backend.aggregation.agents.nxgen.de.banks.n26.authenticator.entities;

import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

@JsonObject
public class TransactionEntity {
    private String id;
    private String userId;
    private String type;
    private double amount;
    private String currencyCode;
    private long visibleTS;
    private boolean recurring;
    private String partnerBic;
    private boolean partnerAccountIsSepa;
    private String partnerName;
    private String accountId;
    private String partnerIban;
    private String category;
    private String referenceText;
    private long userAccepted;
    private long userCertified;
    private boolean pending;
    private String transactionNature;
    private long createdTS;
    private String smartLinkId;
    private String linkId;
    private long confirmed;
    private String partnerBcn;
    private String partnerAccountBan;
    private String smartContactId;
    private String partnerBankName;
    private String referenceToOriginalOperation;
    private String partnerEmail;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public long getVisibleTS() {
        return visibleTS;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public String getPartnerBic() {
        return partnerBic;
    }

    public boolean isPartnerAccountIsSepa() {
        return partnerAccountIsSepa;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getPartnerIban() {
        return partnerIban;
    }

    public String getCategory() {
        return category;
    }

    public String getReferenceText() {
        return referenceText;
    }

    public long getUserAccepted() {
        return userAccepted;
    }

    public long getUserCertified() {
        return userCertified;
    }

    public boolean isPending() {
        return pending;
    }

    public String getTransactionNature() {
        return transactionNature;
    }

    public long getCreatedTS() {
        return createdTS;
    }

    public String getSmartLinkId() {
        return smartLinkId;
    }

    public String getLinkId() {
        return linkId;
    }

    public long getConfirmed() {
        return confirmed;
    }

    public String getPartnerBcn() {
        return partnerBcn;
    }

    public String getPartnerAccountBan() {
        return partnerAccountBan;
    }

    public String getSmartContactId() {
        return smartContactId;
    }

    public String getPartnerBankName() {
        return partnerBankName;
    }

    public String getReferenceToOriginalOperation() {
        return referenceToOriginalOperation;
    }

    public String getPartnerEmail() {
        return partnerEmail;
    }

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(new Amount(getCurrencyCode(),getAmount()))
                .setDate(new Date(getCreatedTS()))
                .setDescription(getReferenceText())
                .setPending(isPending()).build();
    }
}
