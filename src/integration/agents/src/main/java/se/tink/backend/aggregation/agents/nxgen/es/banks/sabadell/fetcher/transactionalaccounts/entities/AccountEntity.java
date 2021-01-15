package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;

@JsonObject
public class AccountEntity {
    private static final Logger log = LoggerFactory.getLogger(AccountEntity.class);
    private String alias;
    private String description;
    private String availability;
    private String owner;
    private String product;
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

    public static Logger getLog() {
        return log;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.from(getTinkAccountType()).get())
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(amount.parseToTinkAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(formatIban(iban))
                                .withAccountName(getTinkName())
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .addHolders(getHolder())
                .setBankIdentifier(iban)
                .putInTemporaryStorage(formatIban(iban), this)
                .build();
    }

    private Holder getHolder() {
        return Holder.of(owner, isOwner ? Role.HOLDER : Role.AUTHORIZED_USER);
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        switch (description.toUpperCase()) {
            case SabadellConstants.AccountTypes.SALARY_ACCOUNT:
            case SabadellConstants.AccountTypes.SALARY_PREMIUM_ACCOUNT:
            case SabadellConstants.AccountTypes.RELATIONSHIP_ACCOUNT:
            case SabadellConstants.AccountTypes.UNDERAGED_ACCOUNT:
                return AccountTypes.CHECKING;
            case SabadellConstants.AccountTypes.MANAGED_ACCOUNT:
            case SabadellConstants.AccountTypes.CURRENCY_ACCOUNT:
            case SabadellConstants.AccountTypes.BUSINESS_EXPANSION_ACCOUNT:
                return AccountTypes.OTHER;
            default:
                log.warn(
                        "{}: Unknown type: {}",
                        SabadellConstants.Tags.UNKNOWN_ACCOUNT_TYPE,
                        description);
                return AccountTypes.OTHER;
        }
    }

    @JsonIgnore
    private String formatIban(String iban) {
        return new DisplayAccountIdentifierFormatter()
                .apply(AccountIdentifier.create(Type.IBAN, iban));
    }

    @JsonIgnore
    private String getTinkName() {
        return !Strings.isNullOrEmpty(alias) ? alias : description;
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
}
