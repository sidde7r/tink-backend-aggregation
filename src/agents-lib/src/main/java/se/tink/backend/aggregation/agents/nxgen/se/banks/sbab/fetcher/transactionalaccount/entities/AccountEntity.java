package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.ACCOUNT_TYPE_MAPPER;

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

    public boolean isTransactionalAccount() {
        return ACCOUNT_TYPE_MAPPER.isTransactionalAccount(accountType);
    }

    private AccountTypes getTinkAccountType() {
        final Optional<AccountTypes> accountType = ACCOUNT_TYPE_MAPPER.translate(getAccountType());

        return accountType.orElse(AccountTypes.OTHER);
    }

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(getTinkAccountType(), accountNumber.toLowerCase())
                .setAccountNumber(accountNumber)
                .setName(getAccountName())
                .setBalance(Amount.inSEK(balance))
                .setBankIdentifier(accountNumber)
                .build();
    }
}
