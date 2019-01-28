package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites;

import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.agents.rpc.AccountTypes;
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

    public TransactionalAccount toTinkAccount() {
       return TransactionalAccount.builder(getTinkAccountType(), accountNumber,
               Amount.inNOK(accountBalance.getAvailableBalance()))
               .setAccountNumber(accountNumber)
               .setName(getProperties().getAlias())
               .setBankIdentifier(id)
               .putInTemporaryStorage(SparebankenSorConstants.Storage.TEMPORARY_STORAGE_LINKS, links)
               .build();
    }

    private AccountTypes getTinkAccountType() {
        if (type == null) {
            return AccountTypes.OTHER;
        }

        switch(type.toLowerCase()) {
        case SparebankenSorConstants.Accounts.CHECKING_ACCOUNT:
            return AccountTypes.CHECKING;
        default:
            LOGGER.warn(String.format(
                    "Could not map account type [%s] to a Tink account type", type));
            return AccountTypes.OTHER;
        }
    }
}
