package se.tink.backend.core;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Preconditions;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.uuid.UUIDUtils;

@Table(value = "loan_data")
public class Loan implements Comparable<Loan>, Serializable {

    public enum Type {
        MORTGAGE, BLANCO, MEMBERSHIP, VEHICLE, LAND, STUDENT, OTHER;

        public static final String DOCUMENTED = "MORTGAGE, BLANCO, MEMBERSHIP, VEHICLE, LAND, STUDENT, OTHER";
    }

    @Tag(1)
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID accountId;
    @Tag(2)
    @AccessType(AccessType.Type.PROPERTY)
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID id;
    @Tag(3)
    private UUID userId;
    @Tag(4)
    private UUID credentialsId;
    @Tag(5)
    private Double initialBalance;
    @Tag(6)
    private Date initialDate;
    @Tag(7)
    private Integer numMonthsBound;
    @Tag(8)
    private String name;
    @Tag(9)
    private Double interest;
    @Tag(10)
    private Double balance;
    @Tag(11)
    private Double amortized;
    @Tag(12)
    private Date nextDayOfTermsChange;
    @Exclude
    private String serializedLoanResponse;
    @Transient
    @Tag(13)
    private Date updated;
    @Tag(14)
    private String providerName;
    @Tag(15)
    private String type;
    @Tag(16)
    private String loanNumber;
    @Tag(17)
    private Double monthlyAmortization;
    @Transient
    @Tag(18)
    private LoanDetails loanDetails;
    @Exclude
    private Boolean userModifiedType;

    public Loan() {
        this.id = UUIDs.timeBased();
    }

    public Loan(Loan toCopy) {
        this.id = UUIDs.timeBased();

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
        return new Long(id.timestamp()).compareTo(new Long(o.id.timestamp()));
    }

    public boolean hasUpdatedSince(Loan prev) {
        Preconditions.checkNotNull(prev);
        Preconditions.checkArgument(accountId.equals(prev.getAccountId()));

        if (balance != null && prev.balance == null) {
            return true;
        }
        if (balance != null && prev.balance != null && balance.doubleValue() != prev.balance.doubleValue()) {
            return true;
        }
        if (interest != null && prev.interest == null) {
            return true;
        }
        if (interest != null && prev.interest != null && interest.doubleValue() != prev.interest.doubleValue()) {
            return true;
        }
        if (amortized != null && prev.amortized == null) {
            return true;
        }
        if (amortized != null && prev.amortized != null && amortized.doubleValue() != prev.amortized.doubleValue()) {
            return true;
        }
        if (name != null && prev.name == null) {
            return true;
        }
        if (name != null && !name.equals(prev.name)) {
            return true;
        }
        if (nextDayOfTermsChange != null && prev.nextDayOfTermsChange == null) {
            return true;
        }
        if (nextDayOfTermsChange != null && prev.nextDayOfTermsChange != null &&
                nextDayOfTermsChange.getTime() != prev.getNextDayOfTermsChange().getTime()) {
            return true;
        }
        if (providerName != null && prev.providerName == null) {
            return true;
        }
        if (providerName != null && !providerName.equals(prev.providerName)) {
            return true;
        }
        if (type != null && prev.type == null) {
            return true;
        }
        if (type != null && !type.equals(prev.type)) {
            return true;
        }
        if (!Objects.equals(loanNumber, prev.getLoanNumber())) {
            return true;
        }
        if (!Objects.equals(monthlyAmortization, prev.getMonthlyAmortization())) {
            return true;
        }
        if (!prev.isUserModifiedType() && isUserModifiedType()) {
            return true;
        }

        return false;
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
                Preconditions.checkArgument(isInMortgageRateInterval(interest),
                        String.format("[credentialsId:%s accountId:%s] Interest rate not in mortgage interval.",
                                credentialsId, accountId));
                break;
            case "BLANCO":
                Preconditions.checkArgument(isInBlancoLoanRateInterval(interest),
                        String.format("[credentialsId:%s accountId:%s] Interest rate not in blanco rate interval.",
                                credentialsId, accountId));
                break;
            case "VEHICLE":
                Preconditions.checkArgument(isInVehicleLoanRateInterval(interest),
                        String.format("[credentialsId:%s accountId:%s] Interest rate not in vehicle rate interval.",
                                credentialsId, accountId));
                break;
            case "STUDENT":
                Preconditions.checkArgument(isInStudentLoanRateInterval(interest),
                        String.format("[credentialsId:%s accountId:%s] Interest rate not in student rate interval.",
                                credentialsId, accountId));
                break;
            default:
                // No matter what all our rates should be in the unit interval.
                Preconditions.checkArgument(isInRateInterval(interest, 1.0),
                        String.format("[credentialsId:%s accountId:%s] Interest rate not in rate interval.",
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

    public Type getType() {
        if (type == null) {
            return null;
        } else {
            return Type.valueOf(type);
        }
    }

    public void setType(Type type) {
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
        // The maxRate value 0.1 is chosen as a threshold to verify that the rate is indeed a mortgage rate.
        // We've chosen a threshold that is as small as possible but considerably larger than the likely values.
        return isInRateInterval(interest, 0.1);
    }

    private boolean isInBlancoLoanRateInterval(Double interest) {
        // The maxRate value 0.35 is chosen as a threshold to verify that the rate is indeed a blanco loan rate.
        // We've chosen a threshold that is as small as possible but considerably larger than the likely values.
        return isInRateInterval(interest, 0.35);
    }

    private boolean isInVehicleLoanRateInterval(Double interest) {
        // The maxRate value 0.3 is chosen as a threshold to verify that the rate is indeed a vehicle loan rate.
        // We've chosen a threshold that is as small as possible but considerably larger than the likely values.
        return isInRateInterval(interest, 0.3);
    }

    private boolean isInStudentLoanRateInterval(Double interest) {
        // The maxRate value 0.3 is chosen as a threshold to verify that the rate is indeed a student loan rate.
        // We've chosen a threshold that is as small as possible but considerably larger than the likely values.
        return isInRateInterval(interest, 0.1);
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getUserModifiedType() {
        return userModifiedType;
    }

    public void setUserModifiedType(Boolean userModifiedType) {
        this.userModifiedType = userModifiedType;
    }
}
