package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class ProductEntity {
    private String id;
    private String type;
    private String identifier;
    private String description;
    private String title;
    @JsonProperty("amount")
    private AmountEntity amountEntity;
    @JsonProperty("accountInfo")
    private AccountInfoEntity accountInfoEntity;
    @JsonProperty("extrasInfo")
    private ExtraInfoEntity extraInfoEntity;
    Logger logger = LoggerFactory.getLogger(ProductEntity.class);

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public AmountEntity getAmountEntity() {
        return amountEntity;
    }

    public AccountInfoEntity getAccountInfoEntity() {
        return accountInfoEntity;
    }

    public ExtraInfoEntity getExtraInfoEntity() {
        return extraInfoEntity;
    }

    private AccountTypes getAccountType() {
        String accountType = getType().toUpperCase();
        switch (accountType) {
        case ErsteBankConstants.ACCOUNTYPE.CHECKING:
            return AccountTypes.CHECKING;
        case ErsteBankConstants.ACCOUNTYPE.SAVINGS:
            return AccountTypes.SAVINGS;
        default:
            logger.warn("{} {}", ErsteBankConstants.LOGTAG.UNKNOWN_ACCOUNT_TYPE, accountType);
            return AccountTypes.OTHER;
        }
    }

    private Amount getTinkBalance() {
        return getAmountEntity().getTinkBalance();
    }

    private String getIban() {
        return identifier.trim();
    }

    private String getName() {
        return getDescription();
    }

    private HolderName getHolderName() {
        return new HolderName(getTitle());
    }

    public TransactionalAccount toTransactionalAccount() {

        return TransactionalAccount.builder(getAccountType(), getIban(), getTinkBalance())
                .setAccountNumber(getIdentifier().trim())
                .setName(getName())
                .setHolderName(getHolderName())
                .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, getIban()))
                .putInTemporaryStorage(ErsteBankConstants.STORAGE.TRANSACTIONSURL, getId())
                .build();
    }

}
