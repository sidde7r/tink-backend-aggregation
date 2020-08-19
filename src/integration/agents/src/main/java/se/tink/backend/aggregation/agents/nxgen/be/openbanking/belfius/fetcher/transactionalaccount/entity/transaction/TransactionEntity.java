package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {

    private BigDecimal amount;
    private String communication;

    @JsonProperty("communication_type")
    private String communicationType;

    @JsonProperty("counterparty_account")
    private String counterpartyAccount;

    @JsonProperty("counterparty_info")
    private String counterpartyInfo;

    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("execution_date")
    private Date executionDate;

    @JsonProperty("remittance_info")
    private String remittanceInfo;

    @JsonProperty("transaction_ref")
    private String transactionRef;

    @JsonCreator
    public TransactionEntity(
            @JsonProperty("communication_type") String communicationType,
            @JsonProperty("counterparty_account") String counterpartyAccount,
            @JsonProperty("counterparty_info") String counterpartyInfo,
            @JsonProperty("currency") String currency,
            @JsonProperty("execution_date") Date executionDate,
            @JsonProperty("remittance_info") String remittanceInfo,
            @JsonProperty("transaction_ref") String transactionRef,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("communication") String communication) {
        this.remittanceInfo = remittanceInfo.replace("\n", " ").replace("\r", " ");
        this.amount = amount;
        this.communicationType = communicationType;
        this.counterpartyAccount = counterpartyAccount;
        this.counterpartyInfo = counterpartyInfo;
        this.currency = currency;
        this.executionDate = executionDate;
        this.transactionRef = transactionRef;
        this.communication = communication;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCommunication() {
        return communication;
    }

    public void setCommunication(String communication) {
        this.communication = communication;
    }

    public String getCommunicationType() {
        return communicationType;
    }

    public void setCommunicationType(String communicationType) {
        this.communicationType = communicationType;
    }

    public String getCounterpartyAccount() {
        return counterpartyAccount;
    }

    public void setCounterpartyAccount(String counterpartyAccount) {
        this.counterpartyAccount = counterpartyAccount;
    }

    public String getCounterpartyInfo() {
        return counterpartyInfo;
    }

    public void setCounterpartyInfo(String counterpartyInfo) {
        this.counterpartyInfo = counterpartyInfo;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public String getRemittanceInfo() {
        return remittanceInfo;
    }

    public void setRemittanceInfo(String remittanceInfo) {
        this.remittanceInfo = remittanceInfo;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(remittanceInfo)
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setDate(executionDate)
                .build();
    }
}
