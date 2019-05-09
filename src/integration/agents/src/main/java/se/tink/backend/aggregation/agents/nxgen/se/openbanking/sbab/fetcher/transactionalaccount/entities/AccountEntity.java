package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("closed_date")
    private String closedDate;

    @JsonProperty("accrued_interest_credit")
    private String accruedInterestCredit;

    @JsonProperty("opened_date")
    private String openedDate;

    private Number balance;

    @JsonProperty("tax_account")
    private String taxAccount;

    @JsonProperty("customer_properties")
    private AccountCustomerProperties customerProperties;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("interest_rate")
    private Number interestRate;

    @JsonProperty("co_owned_account")
    private Boolean coOwnedAccount;

    private String currency;

    private String status;

    public TransactionalAccount toTinkAccount(String customerName) {
        final AccountTypes type =
                SBABConstants.ACCOUNT_TYPE_MAPPER.translate(accountType).orElse(AccountTypes.OTHER);
        switch (type) {
            case SAVINGS:
                return toSavingsAccount(customerName);
            case OTHER:
                return toSavingsAccount(customerName);
            default:
                throw new IllegalStateException(ErrorMessages.UNKNOWN_ACCOUNT_TYPE);
        }
    }

    public TransactionalAccount toSavingsAccount(String customerName) {
        return SavingsAccount.builder()
                .setUniqueIdentifier(accountNumber)
                .setAccountNumber(accountNumber)
                .setBalance(new Amount(currency, balance))
                .setAlias(accountName)
                .addAccountIdentifier(
                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, accountNumber))
                .addHolderName(customerName)
                .putInTemporaryStorage(SBABConstants.StorageKeys.ACCOUNT_NUMBER, accountNumber)
                .setApiIdentifier(accountNumber)
                .build();
    }
}
