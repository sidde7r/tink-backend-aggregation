package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.util.List;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.agents.rpc.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity implements GeneralAccountEntity {
    @JsonProperty("AccountId")
    protected String accountId;
    @JsonProperty("AccountNumber")
    protected String accountNumber;
    @JsonProperty("Address")
    protected String address;
    @JsonProperty("AvailableAmount")
    protected double availableAmount;
    @JsonProperty("BIC")
    protected String bic;
    @JsonProperty("CurrentAmount")
    protected double currentAmount;
    @JsonProperty("CreditLimit")
    protected Double creditLimit;
    @JsonProperty("Holder")
    protected String holder;
    @JsonProperty("IBAN")
    protected String iban;
    @JsonProperty("Name")
    protected String name;
    @JsonProperty("OutstandingAmount")
    protected double outstandingAmount;
    @JsonProperty("Services")
    protected List<String> services;
    @JsonProperty("Transactions")
    protected List<TransactionEntity> transactions;
    @JsonProperty("Type")
    protected String type;
    @JsonProperty("ValidFor")
    protected List<String> validFor;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(double availableAmount) {
        this.availableAmount = availableAmount;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(double outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getValidFor() {
        return validFor;
    }

    public void setValidFor(List<String> validFor) {
        this.validFor = validFor;
    }

    public Account toAccount() {
        Account account = new Account();

        account.setAccountNumber(accountNumber);

        if (Objects.equal(name, name.toUpperCase())) {
            account.setName(StringUtils.formatHuman(name).replace("Ica ", "ICA "));
        } else {
            account.setName(name);
        }

        // default type
        account.setType(AccountTypes.CHECKING);

        if (type != null && Objects.equal("savingsaccount", type.toLowerCase())) {
            account.setType(AccountTypes.SAVINGS);
        } else if (creditLimit != null && !creditLimit.isNaN() && creditLimit > 0) {
            account.setType(AccountTypes.CREDIT_CARD);
        }

        account.setBankId(accountNumber);
        account.putIdentifier(new SwedishIdentifier(accountNumber));

        double availableCredit = Math.floor(availableAmount - currentAmount + outstandingAmount);

        account.setBalance(currentAmount - outstandingAmount);
        account.setAvailableCredit(availableCredit);

        return account;
    }


    /*
     * The methods below are for general purposes
     */

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(getAccountNumber());
    }

    @Override
    public String generalGetBank() {
        if (generalGetAccountIdentifier().isValid()) {
            return generalGetAccountIdentifier().to(SwedishIdentifier.class).getBankName();
        }
        return null;
    }

    @Override
    public String generalGetName() {
        return getName();
    }
}
