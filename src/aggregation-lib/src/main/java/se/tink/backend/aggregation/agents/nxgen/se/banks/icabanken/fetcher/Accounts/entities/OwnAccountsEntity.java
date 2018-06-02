package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class OwnAccountsEntity implements GeneralAccountEntity {
    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(OwnAccountsEntity.class);

    @JsonProperty("Type")
    private String type;
    @JsonProperty("AccountId")
    private String accountId;
    @JsonProperty("AccountNumber")
    private String accountNumber;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("AvailableAmount")
    private double availableAmount;
    @JsonProperty("CurrentAmount")
    private double currentAmount;
    @JsonProperty("OutstandingAmount")
    private double outstandingAmount;
    @JsonProperty("CreditLimit")
    private double creditLimit;
    @JsonProperty("ValidFor")
    private List<String> validFor;
    @JsonProperty("IBAN")
    private String iban;
    @JsonProperty("BIC")
    private String bic;
    @JsonProperty("Address")
    private String address;
    @JsonProperty("Holder")
    private String holder;
    @JsonProperty("Services")
    private List<String> services;
    @JsonProperty("AccountOwner")
    private AccountOwnerEntity accountOwner;


    public String getType() {
        return type;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getName() {
        return name;
    }

    public double getAvailableAmount() {
        return availableAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public double getOutstandingAmount() {
        return outstandingAmount;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    @JsonProperty("ValidFor")
    public List<String> getValidFor() {
        return validFor;
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public String getAddress() {
        return address;
    }

    public String getHolder() {
        return holder;
    }

    public List<String> getServices() {
        return services;
    }

    public AccountOwnerEntity getAccountOwner() {
        return accountOwner;
    }

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(convertAccountType(), accountNumber,
                new Amount(IcaBankenConstants.Currencies.SEK, getAvailableAmount())).setName(name)
                .setBankIdentifier(accountId).addIdentifier(new SwedishIdentifier(getAccountNumber())).build();
    }

    private AccountTypes convertAccountType() {
        IcaBankenConstants.AccountType accountTypeEnum = IcaBankenConstants.AccountType.toAccountType(type);

        if (accountTypeEnum == IcaBankenConstants.AccountType.UNKOWN) {
            logger.info("Unknown account type. Logging account of type: {}", accountTypeEnum);

            return AccountTypes.CHECKING;
        }

        return accountTypeEnum.getTinkType();
    }

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
