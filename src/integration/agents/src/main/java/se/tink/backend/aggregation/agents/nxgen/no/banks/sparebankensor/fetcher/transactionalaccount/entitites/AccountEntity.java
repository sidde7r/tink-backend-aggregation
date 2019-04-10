package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.HashMap;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    private static final AggregationLogger LOGGER = new AggregationLogger(AccountEntity.class);

    private String id;
    private String accountNumber;
    private String customerRole;
    private String type;
    private OwnerEntity owner;
    private PropertiesEntity properties;
    private AccountBalanceEntity accountBalance;
    private RightsEntity rights;
    private HashMap<String, LinkEntity> links;

    public String getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCustomerRole() {
        return customerRole;
    }

    public String getType() {
        return type;
    }

    public OwnerEntity getOwner() {
        return owner;
    }

    public PropertiesEntity getProperties() {
        return properties;
    }

    public AccountBalanceEntity getAccountBalance() {
        return accountBalance;
    }

    public RightsEntity getRights() {
        return rights;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return SparebankenSorConstants.Accounts.CHECKING_ACCOUNT.equalsIgnoreCase(type)
                || SparebankenSorConstants.Accounts.SAVINGS_ACCOUNT.equalsIgnoreCase(type);
    }

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(
                        getTinkAccountType(),
                        accountNumber,
                        getBalance(accountBalance.getAvailableBalance()))
                .setAccountNumber(accountNumber)
                .setName(getProperties().getAlias())
                .setHolderName(new HolderName(owner.getName()))
                .setBankIdentifier(id)
                .putInTemporaryStorage(
                        SparebankenSorConstants.Storage.TEMPORARY_STORAGE_LINKS, links)
                .build();
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {

        switch (type.toLowerCase()) {
            case SparebankenSorConstants.Accounts.CHECKING_ACCOUNT:
                return AccountTypes.CHECKING;
            case SparebankenSorConstants.Accounts.SAVINGS_ACCOUNT:
                return AccountTypes.SAVINGS;
            default:
                // This should never happen as we filter on checking and savings accounts
                LOGGER.warn(
                        String.format(
                                "Could not map account type [%s] to a Tink account type", type));
                return AccountTypes.OTHER;
        }
    }

    @JsonIgnore
    public boolean isLoanAccount() {
        return SparebankenSorConstants.Accounts.LOAN.equalsIgnoreCase(type);
    }

    // Currently logging loan details.
    @JsonIgnore
    public LoanAccount toTinkLoan() {
        return LoanAccount.builder(accountNumber, getBalance(accountBalance.getAccountingBalance()))
                .setAccountNumber(accountNumber)
                .setName(properties.getAlias())
                .setHolderName(new HolderName(owner.getName()))
                .build();
    }

    @JsonIgnore
    private Amount getBalance(double balance) {
        String currency = properties.getCurrencyCode();

        if (Strings.isNullOrEmpty(currency)) {
            LOGGER.warn("Sparebanken Sor: No currency for account found. Defaulting to NOK.");

            return Amount.inNOK(balance);
        }

        return new Amount(currency, balance);
    }
}
