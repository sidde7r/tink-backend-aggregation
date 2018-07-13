package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.util.Date;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapDeserializer;
import se.tink.libraries.date.ThreadSafeDateFormat;

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

    public Double getGranted() {
        if (!Strings.isNullOrEmpty(granted)) {
            return AgentParsingUtils.parseAmount(granted);
        }
        return null;
    }

    public void setGranted(String granted) {
        this.granted = granted;
    }

    public Double getBalance() {
        if (!Strings.isNullOrEmpty(balance)) {
            return AgentParsingUtils.parseAmount(balance);
        }
        return null;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public Date getInterestTermEnds() throws ParseException {
        if (!Strings.isNullOrEmpty(interestTermEnds) && interestTermEnds.length() >= 10) {
            return ThreadSafeDateFormat.FORMATTER_DAILY.parse(interestTermEnds.substring(0, 10));
        }
        return null;
    }

    public void setInterestTermEnds(String interestTermEnds) {
        this.interestTermEnds = interestTermEnds;
    }

    public Double getInterest() {
        if (!Strings.isNullOrEmpty(interest)) {
            return AgentParsingUtils.parsePercentageFormInterest(interest);
        }
        return null;
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
