package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;
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

    @JsonProperty("cardInfo")
    private CardInfoEntity cardInfoEntity;

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
        case ErsteBankConstants.ACCOUNTYPE.BUILDING_SAVING:
        case ErsteBankConstants.ACCOUNTYPE.SAVING:
            return AccountTypes.SAVINGS;
        default:
            logger.warn("{} {}", ErsteBankConstants.LOGTAG.UNKNOWN_ACCOUNT_TYPE, accountType);
            throw new IllegalStateException("Unknown account type");
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

    public boolean isValidTransactional() {
        try {
            toTransactionalAccount();
            return true;
        } catch (Exception e) {
            logger.warn("{} {}", ErsteBankConstants.LOGTAG.TRANSANSACTIONAL_ACC_ERR, getType());
            return false;
        }
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

    public boolean isCreditCardAccount() {
        return ErsteBankConstants.ACCOUNTYPE.CARD_CREDIT.equalsIgnoreCase(getType());
    }

    public boolean isValidCreditCardAccount() {
        try {
            toCreditCardAccount();
            return true;
        } catch (Exception e) {
            logger.warn("{} {}", ErsteBankConstants.LOGTAG.CREDIT_ACC_ERR, e.toString());
            return false;
        }
    }

    private Amount getAvailableCredit() {
        return cardInfoEntity.getAvailableAmount().getTinkBalance();
    }

    public CreditCardAccount toCreditCardAccount() {
        return CreditCardAccount.builder(id, getTinkBalance(), getAvailableCredit())
                .setAccountNumber(identifier)
                .setName(description)
                .setHolderName(getHolderName())
                .putInTemporaryStorage(ErsteBankConstants.STORAGE.CREDITURL, getId())
                .build();
    }

}
