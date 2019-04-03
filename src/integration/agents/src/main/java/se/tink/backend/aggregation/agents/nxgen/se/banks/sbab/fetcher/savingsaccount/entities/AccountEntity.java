package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_type")
    private String accountType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("closed_date")
    private Date closedDate;

    @JsonProperty("balance")
    private long balance;

    @JsonProperty("tax_account")
    private boolean taxAccount;

    @JsonProperty("customer_properties")
    private CustomerPropertiesEntity customerProperties;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("interest_rate")
    private double interestRate;

    @JsonProperty("co_owned_account")
    private boolean coOwnedAccount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("opened_date")
    private Date openedDate;

    @JsonProperty("status")
    private String status;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public Date getClosedDate() {
        return closedDate;
    }

    public long getBalance() {
        return balance;
    }

    public boolean isTaxAccount() {
        return taxAccount;
    }

    public CustomerPropertiesEntity getCustomerProperties() {
        return customerProperties;
    }

    public String getAccountName() {
        return accountName;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public boolean isCoOwnedAccount() {
        return coOwnedAccount;
    }

    public Date getOpenedDate() {
        return openedDate;
    }

    public String getStatus() {
        return status;
    }

    public boolean isSavingsAccount() {
        return getTinkAccountType().equals(AccountTypes.SAVINGS);
    }

    private AccountTypes getTinkAccountType() {
        return ACCOUNT_TYPE_MAPPER.translate(getAccountType()).orElse(AccountTypes.OTHER);
    }

    public SavingsAccount toTinkAccount() {
        return SavingsAccount.builder()
            .setUniqueIdentifier(accountNumber.toLowerCase())
                .setAccountNumber(accountNumber)
                .setBalance(Amount.inSEK(balance))
                .setAlias(getAccountName())
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.SE, accountNumber))
                .build();
    }
}
