package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities;

import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.AccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
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

    public String getTransactionUrl() {
        return links.get(HandelsbankenNOConstants.Tags.TRANSACTIONS).getHref();
    }

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(getTinkAccountType(), this.accountNumber,
                Amount.inNOK(this.accountBalance.getAccountingBalance()))
                .setAccountNumber(this.accountNumber)
                .setName(this.properties.getAlias())
                .setBankIdentifier(getTransactionUrl())
                .build();
    }

    private AccountTypes getTinkAccountType() {
        switch (this.type.toLowerCase()) {
        case AccountType.SPENDING:
            return AccountTypes.CHECKING;
        case AccountType.SAVING:
        case AccountType.YOUTH_SAVING:
            return AccountTypes.SAVINGS;
        default:
            return AccountTypes.OTHER;
        }
    }

}
