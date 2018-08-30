package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class AccountEntity {
    private static final Logger log = LoggerFactory.getLogger(AccountEntity.class);

    private String alias;
    private String description;
    private String availability;
    private String owner;
    private String product;
    private String productType;
    private String entityCode;
    private String contractCode;
    private String bic;
    private String number;
    private String iban;
    private AmountEntity amount;
    private int numOwners;
    private boolean isOwner;
    private boolean isSBPManaged;
    private boolean isIberSecurities;
    private String joint;
    private String mobileWarning;
    private String contractNumberFormatted;

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(getTinkAccountType(), iban, amount.parseToTinkAmount())
                .setAccountNumber(iban)
                .setName(getTinkName())
                .setBankIdentifier(iban)
                .addIdentifier(new IbanIdentifier(iban))
                .putInTemporaryStorage(iban, this)
                .build();
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        switch (description.toUpperCase()) {
        case SabadellConstants.AccountTypes.SALARY_ACCOUNT:
        case SabadellConstants.AccountTypes.CUENTA_RELACION:
            return AccountTypes.CHECKING;
        default:
            log.warn("{}: Unknown type: {}", SabadellConstants.Tags.UNKNOWN_ACCOUNT_TYPE, description);
            return AccountTypes.OTHER;
        }
    }

    @JsonIgnore
    private String getTinkName() {
        return !Strings.isNullOrEmpty(alias) ? alias : description;
    }

    public static Logger getLog() {
        return log;
    }

    public String getAlias() {
        return alias;
    }

    public String getDescription() {
        return description;
    }

    public String getAvailability() {
        return availability;
    }

    public String getOwner() {
        return owner;
    }

    public String getProduct() {
        return product;
    }

    public String getProductType() {
        return productType;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public String getContractCode() {
        return contractCode;
    }

    public String getBic() {
        return bic;
    }

    public String getNumber() {
        return number;
    }

    public String getIban() {
        return iban;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public int getNumOwners() {
        return numOwners;
    }

    @JsonProperty("isSBPManaged")
    public boolean isSBPManaged() {
        return isSBPManaged;
    }

    @JsonProperty("isIberSecurities")
    public boolean isIberSecurities() {
        return isIberSecurities;
    }

    public String getJoint() {
        return joint;
    }

    public String getMobileWarning() {
        return mobileWarning;
    }

    public String getContractNumberFormatted() {
        return contractNumberFormatted;
    }
}
