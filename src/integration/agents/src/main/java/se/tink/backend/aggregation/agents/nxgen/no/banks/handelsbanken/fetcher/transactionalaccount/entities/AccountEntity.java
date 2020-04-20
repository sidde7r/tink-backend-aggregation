package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountEntity.class);

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

    private String getTransactionUrl() {
        return links.get(HandelsbankenNOConstants.Tags.TRANSACTIONS).getHref();
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return HandelsbankenNOConstants.AccountType.SPENDING.equalsIgnoreCase(type)
                || HandelsbankenNOConstants.AccountType.SAVING.equalsIgnoreCase(type)
                || HandelsbankenNOConstants.AccountType.YOUTH_SAVING.equalsIgnoreCase(type);
    }

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(
                        getTinkAccountType(),
                        accountNumber,
                        getBalance(accountBalance.getAvailableBalance()))
                .setAccountNumber(accountNumber)
                .setName(properties.getAlias())
                .setHolderName(new HolderName(owner.getName()))
                .setBankIdentifier(getTransactionUrl())
                .build();
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        switch (type.toLowerCase()) {
            case HandelsbankenNOConstants.AccountType.SPENDING:
                return AccountTypes.CHECKING;
            case HandelsbankenNOConstants.AccountType.SAVING:
            case HandelsbankenNOConstants.AccountType.YOUTH_SAVING:
                return AccountTypes.SAVINGS;
            default:
                // This should never happen as we filter on checking and savings accounts
                LOGGER.warn("Could not map account type [{}] to a Tink account type", type);
                return AccountTypes.OTHER;
        }
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance(double balance) {
        String currency = properties.getCurrencyCode();

        if (Strings.isNullOrEmpty(currency)) {
            LOGGER.warn("Handelsbanken Norway: No currency for account found. Defaulting to NOK.");

            return ExactCurrencyAmount.of(balance, "NOK");
        }

        return ExactCurrencyAmount.of(balance, currency);
    }
}
