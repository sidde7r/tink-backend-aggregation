package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class LoanDetailsEntity {
    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date dueDate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    @JsonProperty("dueAmount")
    private Double amount;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentStatus;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentStatusText;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String loanType;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private Double interestRate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date nextDueDate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date entryDate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date maturityDate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String interestType;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String terminsPerYear;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String accountForRepayments;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String gracePeriod;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date gracePeriodEndDate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String nickName;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private Double balance;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private Double fundsAvailable;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String currency;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date interestDueDate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date nextInstallment;

    private List<HashMap<String, Object>> accountRoles;
    private LoanData loanData;
    private LoanPaymentDetails followingPayment;
    private LoanPaymentDetails latestPayment;

    public LoanPaymentDetails getFollowingPayment() {
        return followingPayment;
    }

    public LoanPaymentDetails getLatestPayment() {
        return latestPayment;
    }

    public Optional<LoanData> getLoanData() {
        return Optional.ofNullable(loanData);
    }

    public Date getDueDate() {
        return dueDate;
    }

    public Double getAmount() {
        return amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getPaymentStatusText() {
        return paymentStatusText;
    }

    public String getLoanType() {
        return loanType;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public Date getNextDueDate() {
        return nextDueDate;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public Date getMaturityDate() {
        return maturityDate;
    }

    public String getInterestType() {
        return interestType;
    }

    public String getTerminsPerYear() {
        return terminsPerYear;
    }

    public String getAccountForRepayments() {
        return accountForRepayments;
    }

    public String getGracePeriod() {
        return gracePeriod;
    }

    public Date getGracePeriodEndDate() {
        return gracePeriodEndDate;
    }

    public String getNickName() {
        return nickName;
    }

    public Double getBalance() {
        return balance;
    }

    public Amount getBalanceAmount() {
        return new Amount(currency, balance);
    }

    public Double getFundsAvailable() {
        return fundsAvailable;
    }

    public String getCurrency() {
        return currency;
    }

    public Date getInterestDueDate() {
        return interestDueDate;
    }

    public Date getNextInstallment() {
        return nextInstallment;
    }

    public List<HashMap<String, Object>> getAccountRoles() {
        return accountRoles;
    }
}
