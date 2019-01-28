package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.banks.crosskey.utils.CrossKeyUtils;
import se.tink.backend.agents.rpc.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountResponse {
    private String accountNumber;
    private String bban;
    private String bbanFormatted;
    private String accountId;
    private String accountNickname;
    private Double availableAmount;
    private Double balance;
    private String bic;
    private String accountGroup;
    private String accountTypeName;
    private Integer accountType;
    private String currency;
    private String usageType;
    private Double creditLimit;
    private Double interestRate;
    private Double trueInterestRate;
    private Double interestMargin;
    private String capitalization;
    private Double minInterestRate;
    private Double maxInterestRate;
    private String referenceInterestName;
    private Double referenceInterestValue;
    private String accountOwnerName;
    private String accountCoOwnerName;
    private Boolean moreOwnersThanTwo;
    private Boolean owner;
    private Boolean softLocked;
    private Boolean pledged;
    private List<InterestLadder> interestLadder;


    public Account toTinkAccount(CrossKeyConfig config) {
        Account tinkAccount = new Account();

        AccountTypes accountType = config.getAccountType(accountGroup, usageType);
        List<AccountIdentifier> identifiers = config.getIdentifiers(bic, accountNumber, bban);

        for (AccountIdentifier identifier : identifiers) {
            tinkAccount.putIdentifier(identifier);
        }

        tinkAccount.setAccountNumber(getBbanFormatted());
        tinkAccount.setName(getAccountNickname());
        tinkAccount.setBalance(balance);
        tinkAccount.setType(accountType);
        tinkAccount.setAvailableCredit(availableAmount);
        tinkAccount.setBankId(accountId);

        return tinkAccount;
    }

    public String getBbanFormatted() {
        return bbanFormatted;
    }

    public String getAccountNickname() {
        String name = accountTypeName;

        String nickname = CrossKeyUtils.removeSpaces(accountNickname);
        String accountNum = CrossKeyUtils.removeSpaces(accountNumber);
        String bban = CrossKeyUtils.removeSpaces(bbanFormatted);

        if (!nickname.equals(accountNum) && !nickname.equals(bban)) {
            name = accountNickname;
        }

        return StringUtils.formatHuman(name);
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    public void setBban(String bban) {
        this.bban = bban;
    }

    public void setBbanFormatted(String bban) {
        bbanFormatted = bban;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setAccountNickname(String accountNickname) {
        this.accountNickname = accountNickname;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setAvailableAmount(Double availableAmount) {
        this.availableAmount = availableAmount;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public void setAccountGroup(String accountGroup) {
        this.accountGroup = accountGroup;
    }

    public void setAccountTypeName(String accountTypeName) {
        this.accountTypeName = accountTypeName;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBban() {
        return bban;
    }

    public Double getAvailableAmount() {
        return availableAmount;
    }

    public Double getBalance() {
        return balance;
    }

    public String getBic() {
        return bic;
    }

    public String getAccountGroup() {
        return accountGroup;
    }

    public String getAccountTypeName() {
        return accountTypeName;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public String getUsageType() {
        return usageType;
    }

    public Double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(Double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Double getTrueInterestRate() {
        return trueInterestRate;
    }

    public void setTrueInterestRate(Double trueInterestRate) {
        this.trueInterestRate = trueInterestRate;
    }

    public Double getInterestMargin() {
        return interestMargin;
    }

    public void setInterestMargin(Double interestMargin) {
        this.interestMargin = interestMargin;
    }

    public String getCapitalization() {
        return capitalization;
    }

    public void setCapitalization(String capitalization) {
        this.capitalization = capitalization;
    }

    public Double getMinInterestRate() {
        return minInterestRate;
    }

    public void setMinInterestRate(Double minInterestRate) {
        this.minInterestRate = minInterestRate;
    }

    public Double getMaxInterestRate() {
        return maxInterestRate;
    }

    public void setMaxInterestRate(Double maxInterestRate) {
        this.maxInterestRate = maxInterestRate;
    }

    public String getReferenceInterestName() {
        return referenceInterestName;
    }

    public void setReferenceInterestName(String referenceInterestName) {
        this.referenceInterestName = referenceInterestName;
    }

    public Double getReferenceInterestValue() {
        return referenceInterestValue;
    }

    public void setReferenceInterestValue(Double referenceInterestValue) {
        this.referenceInterestValue = referenceInterestValue;
    }

    public String getAccountOwnerName() {
        return accountOwnerName;
    }

    public void setAccountOwnerName(String accountOwnerName) {
        this.accountOwnerName = accountOwnerName;
    }

    public String getAccountCoOwnerName() {
        return accountCoOwnerName;
    }

    public void setAccountCoOwnerName(String accountCoOwnerName) {
        this.accountCoOwnerName = accountCoOwnerName;
    }

    public Boolean getMoreOwnersThanTwo() {
        return moreOwnersThanTwo;
    }

    public void setMoreOwnersThanTwo(Boolean moreOwnersThanTwo) {
        this.moreOwnersThanTwo = moreOwnersThanTwo;
    }

    public Boolean getOwner() {
        return owner;
    }

    public void setOwner(Boolean owner) {
        this.owner = owner;
    }

    public Boolean getSoftLocked() {
        return softLocked;
    }

    public void setSoftLocked(Boolean softLocked) {
        this.softLocked = softLocked;
    }

    public Boolean getPledged() {
        return pledged;
    }

    public void setPledged(Boolean pledged) {
        this.pledged = pledged;
    }

    public List<InterestLadder> getInterestLadder() {
        if (interestLadder == null) {
            return Collections.emptyList();
        }

        return interestLadder;
    }

    public void setInterestLadder(List<InterestLadder> interestLadder) {
        this.interestLadder = interestLadder;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accountType", accountType)
                .add("accountGroup", accountGroup)
                .add("accountTypeName", accountTypeName)
                .add("usageType", usageType)
                .add("interestRate", interestRate)
                .add("trueInterestRate", trueInterestRate)
                .add("interestMargin", interestMargin)
                .add("capitalization", capitalization)
                .add("minInterestRate", minInterestRate)
                .add("maxInterestRate", maxInterestRate)
                .add("referenceInterestName", referenceInterestName)
                .add("referenceInterestValue", referenceInterestValue)
                .add("owner", owner)
                .add("interestLadder", interestLadder)
                .add("lessThanMinus1M", balance < -1000000)
                .add("currency", currency)
                .toString();
    }
}
