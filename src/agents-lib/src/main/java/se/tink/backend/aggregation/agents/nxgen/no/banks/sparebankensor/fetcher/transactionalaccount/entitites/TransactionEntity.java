package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites;

import java.util.Date;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {
    private String id;
    private String accountNumber;
    private boolean reserved;
    private String type;
    private Integer typeCode;
    private String typeText;
    private Double amount;
    private AmountsEntity amounts;
    private String description;
    private Date accountingDate;
    private HashMap<String, LinkEntity> links;
    private Integer sequenceNumber;

    public void setAccountingDate(Date accountingDate) {
        this.accountingDate = accountingDate;
    }

    public String getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public boolean getReserved() {
        return reserved;
    }

    public String getType() {
        return type;
    }

    public Integer getTypeCode() {
        return typeCode;
    }

    public String getTypeText() {
        return typeText;
    }

    public Double getAmount() {
        return amount;
    }

    public AmountsEntity getAmounts() {
        return amounts;
    }

    public String getDescription() {
        return description;
    }

    public Date getAccountingDate() {
        return accountingDate;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public Transaction toTinkTransaction() {
        Transaction.Builder transactionBuilder = Transaction.builder()
                .setDescription(description)
                .setAmount(Amount.inNOK(amount))
                .setDate(accountingDate)
                .setPending(reserved);

        return transactionBuilder.build();
    }
}
