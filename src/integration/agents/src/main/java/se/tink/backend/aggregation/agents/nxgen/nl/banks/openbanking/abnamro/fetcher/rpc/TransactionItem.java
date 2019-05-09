package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.math.BigDecimal;
import java.util.List;

@JsonObject
public class TransactionItem {

    @JsonProperty("mutationCode")
    private String mutationCode;

    @JsonProperty("descriptionLines")
    private List<String> descriptionLines;

    @JsonProperty("bookDate")
    private String bookDate;

    @JsonProperty("balanceAfterMutation")
    private int balanceAfterMutation;

    @JsonProperty("counterPartyAccountNumber")
    private String counterPartyAccountNumber;

    @JsonProperty("counterPartyName")
    private String counterPartyName;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("transactionId")
    private String transactionId;

    public String getMutationCode() {
        return mutationCode;
    }

    public void setMutationCode(String mutationCode) {
        this.mutationCode = mutationCode;
    }

    public List<String> getDescriptionLines() {
        return descriptionLines;
    }

    public void setDescriptionLines(List<String> descriptionLines) {
        this.descriptionLines = descriptionLines;
    }

    public String getBookDate() {
        return bookDate;
    }

    public void setBookDate(String bookDate) {
        this.bookDate = bookDate;
    }

    public int getBalanceAfterMutation() {
        return balanceAfterMutation;
    }

    public void setBalanceAfterMutation(int balanceAfterMutation) {
        this.balanceAfterMutation = balanceAfterMutation;
    }

    public String getCounterPartyAccountNumber() {
        return counterPartyAccountNumber;
    }

    public void setCounterPartyAccountNumber(String counterPartyAccountNumber) {
        this.counterPartyAccountNumber = counterPartyAccountNumber;
    }

    public String getCounterPartyName() {
        return counterPartyName;
    }

    public void setCounterPartyName(String counterPartyName) {
        this.counterPartyName = counterPartyName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}