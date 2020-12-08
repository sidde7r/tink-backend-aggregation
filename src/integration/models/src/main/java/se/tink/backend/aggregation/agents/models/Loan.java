package se.tink.backend.aggregation.agents.models;

import com.fasterxml.uuid.Generators;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;

public class Loan implements Comparable<Loan>, Serializable {
    public enum Type {
        MORTGAGE,
        BLANCO,
        MEMBERSHIP,
        VEHICLE,
        LAND,
        STUDENT,
        CREDIT,
        OTHER
    }

    private UUID accountId;
    private UUID id;
    private UUID userId;
    private UUID credentialsId;
    private Double initialBalance;
    private Date initialDate;
    private Integer numMonthsBound;
    private String name;
    private Double interest;
    private Double balance;
    private Double amortized;
    private Date nextDayOfTermsChange;
    private String serializedLoanResponse;
    private Date updated;
    private String providerName;
    private String type;
    private String loanNumber;
    private Double monthlyAmortization;
    private LoanDetails loanDetails;
    private Boolean userModifiedType;

    public Loan() {
        this.id = Generators.timeBasedGenerator().generate();
    }

    public Loan(Loan toCopy) {
        this.id = Generators.timeBasedGenerator().generate();

        setAccountId(toCopy.getAccountId());
        setAmortized(toCopy.getAmortized());
        setBalance(toCopy.getBalance());
        setCredentialsId(toCopy.getCredentialsId());
        setInitialBalance(toCopy.getInitialBalance());
        setInitialDate(toCopy.getInitialDate());
        setInterest(toCopy.getInterest());
        setName(toCopy.getName());
        setNextDayOfTermsChange(toCopy.getNextDayOfTermsChange());
        setNumMonthsBound(toCopy.getNumMonthsBound());
        setProviderName(toCopy.getProviderName());
        setSerializedLoanResponse(toCopy.getSerializedLoanResponse());
        setType(toCopy.getType());
        setUpdated(toCopy.getUpdated());
        setUserId(toCopy.getUserId());
        setLoanNumber(toCopy.getLoanNumber());
        setMonthlyAmortization(toCopy.getMonthlyAmortization());
        setLoanDetails(toCopy.getLoanDetails());
        setUserModifiedType(toCopy.isUserModifiedType());
    }

    @Override
    public int compareTo(Loan o) {
        return Long.compare(id.timestamp(), o.id.timestamp());
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
        updated = UUIDUtils.UUIDToDate(id);
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(UUID credentialsId) {
        this.credentialsId = credentialsId;
    }

    public Double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(Double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public Date getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(Date initialDate) {
        this.initialDate = initialDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getInterest() {
        return interest;
    }

    public void setInterest(Double interest) {
        if (interest == null) {
            return;
        }
        if (type != null) {
            switch (type) {
                case "MORTGAGE":
                    Preconditions.checkArgument(
                            isInMortgageRateInterval(interest),
                            String.format(
                                    "[credentialsId:%s accountId:%s] Interest rate not in mortgage interval.",
                                    credentialsId, accountId));
                    break;
                case "BLANCO":
                    Preconditions.checkArgument(
                            isInBlancoLoanRateInterval(interest),
                            String.format(
                                    "[credentialsId:%s accountId:%s] Interest rate not in blanco rate interval.",
                                    credentialsId, accountId));
                    break;
                case "VEHICLE":
                    Preconditions.checkArgument(
                            isInVehicleLoanRateInterval(interest),
                            String.format(
                                    "[credentialsId:%s accountId:%s] Interest rate not in vehicle rate interval.",
                                    credentialsId, accountId));
                    break;
                case "STUDENT":
                    Preconditions.checkArgument(
                            isInStudentLoanRateInterval(interest),
                            String.format(
                                    "[credentialsId:%s accountId:%s] Interest rate not in student rate interval.",
                                    credentialsId, accountId));
                    break;
                default:
                    // No matter what all our rates should be in the unit interval.
                    Preconditions.checkArgument(
                            isInRateInterval(interest, 1.0),
                            String.format(
                                    "[credentialsId:%s accountId:%s] Interest rate not in rate interval.",
                                    credentialsId, accountId));
            }
        }

        this.interest = interest;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Date getNextDayOfTermsChange() {
        return nextDayOfTermsChange;
    }

    public void setNextDayOfTermsChange(Date nextDayOfTermsChange) {
        this.nextDayOfTermsChange = nextDayOfTermsChange;
    }

    public Double getAmortized() {
        return amortized;
    }

    public void setAmortized(Double amortized) {
        this.amortized = amortized;
    }

    public Integer getNumMonthsBound() {
        return numMonthsBound;
    }

    public void setNumMonthsBound(Integer numMonthsBound) {
        this.numMonthsBound = numMonthsBound;
    }

    public String getSerializedLoanResponse() {
        return serializedLoanResponse;
    }

    public void setSerializedLoanResponse(String serializedLoanResponse) {
        this.serializedLoanResponse = serializedLoanResponse;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    public Double getMonthlyAmortization() {
        return monthlyAmortization;
    }

    public void setMonthlyAmortization(Double monthlyAmortization) {
        this.monthlyAmortization = monthlyAmortization;
    }

    public LoanDetails getLoanDetails() {
        return loanDetails;
    }

    public void setLoanDetails(LoanDetails loanDetails) {
        this.loanDetails = loanDetails;
    }

    public Loan.Type getType() {
        if (type == null) {
            return null;
        } else {
            return Loan.Type.valueOf(type);
        }
    }

    public void setType(Loan.Type type) {
        if (type == null) {
            this.type = null;
        } else {
            this.type = type.toString();
        }
    }

    public boolean isUserModifiedType() {
        if (userModifiedType == null) {
            return false;
        }
        return userModifiedType;
    }

    public void setUserModifiedType(boolean userModifiedType) {
        this.userModifiedType = userModifiedType;
    }

    private boolean isInRateInterval(Double interest, Double maxRate) {
        if (interest == null) {
            return true;
        }

        if (maxRate != null) {
            if (interest >= 0.0 && interest <= maxRate) {
                return true;
            }
        }

        return false;
    }

    private boolean isInMortgageRateInterval(Double interest) {
        // The maxRate value 0.1 is chosen as a threshold to verify that the rate is indeed a
        // mortgage rate.
        // We've chosen a threshold that is as small as possible but considerably larger than the
        // likely values.
        return isInRateInterval(interest, 0.1);
    }

    private boolean isInBlancoLoanRateInterval(Double interest) {
        // The maxRate value 0.35 is chosen as a threshold to verify that the rate is indeed a
        // blanco loan rate.
        // We've chosen a threshold that is as small as possible but considerably larger than the
        // likely values.
        return isInRateInterval(interest, 0.35);
    }

    private boolean isInVehicleLoanRateInterval(Double interest) {
        // The maxRate value 0.3 is chosen as a threshold to verify that the rate is indeed a
        // vehicle loan rate.
        // We've chosen a threshold that is as small as possible but considerably larger than the
        // likely values.
        return isInRateInterval(interest, 0.3);
    }

    private boolean isInStudentLoanRateInterval(Double interest) {
        // The maxRate value 0.3 is chosen as a threshold to verify that the rate is indeed a
        // student loan rate.
        // We've chosen a threshold that is as small as possible but considerably larger than the
        // likely values.
        return isInRateInterval(interest, 0.1);
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accountId", accountId)
                .add("id", id)
                .add("userId", userId)
                .add("credentialsId", credentialsId)
                .add("initialBalance", initialBalance == null ? null : "***")
                .add("initialDate", initialDate == null ? null : "***")
                .add("numMonthsBound", numMonthsBound)
                .add("name", name)
                .add("interest", interest)
                .add("balance", balance == null ? null : "***")
                .add("amortized", amortized)
                .add("nextDayOfTermsChange", nextDayOfTermsChange)
                .add("serializedLoanResponse", serializedLoanResponse)
                .add("updated", updated)
                .add("providerName", providerName)
                .add("type", type)
                .add("loanNumber", loanNumber)
                .add("monthlyAmortization", monthlyAmortization)
                .add("loanDetails", loanDetails)
                .add("userModifiedType", userModifiedType)
                .toString();
    }
}
