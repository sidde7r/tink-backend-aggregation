package se.tink.backend.aggregation.agents.banks.nordea.v18.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanData {

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String localNumber;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String currency;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String granted;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String balance;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String interestTermEnds;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String interest;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentAccount;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentFrequency;

    public String getLocalNumber() {
        return localNumber;
    }

    public void setLocalNumber(String localNumber) {
        this.localNumber = localNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getGranted() {
        return granted;
    }

    public void setGranted(String granted) {
        this.granted = granted;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getInterestTermEnds() {
        return interestTermEnds;
    }

    public void setInterestTermEnds(String interestTermEnds) {
        this.interestTermEnds = interestTermEnds;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getPaymentAccount() {
        return paymentAccount;
    }

    public void setPaymentAccount(String paymentAccount) {
        this.paymentAccount = paymentAccount;
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }
}
