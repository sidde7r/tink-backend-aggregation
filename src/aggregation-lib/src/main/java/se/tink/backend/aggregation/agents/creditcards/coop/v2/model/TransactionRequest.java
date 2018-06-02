package se.tink.backend.aggregation.agents.creditcards.coop.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionRequest extends BaseRequest {
    private int maxNrOfTransactions;
    private int accountType;
    private int fromYear;

    public int getMaxNrOfTransactions() {
        return maxNrOfTransactions;
    }

    public void setMaxNrOfTransactions(int maxNrOfTransactions) {
        this.maxNrOfTransactions = maxNrOfTransactions;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public int getFromYear() {
        return fromYear;
    }

    public void setFromYear(int fromYear) {
        this.fromYear = fromYear;
    }
}
