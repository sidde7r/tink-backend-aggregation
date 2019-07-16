package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class TransactionAccountEntity {

    private String cashAccountType;
    private String resourceId;

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    private String accountType;
    private String iban;
    private String name;
    private String currency;
    private String bic;

    public String getCashAccountType() {
        return cashAccountType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public LinksEntity getLinks() {
        return linksEntity;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getIban() {
        return iban;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBic() {
        return bic;
    }

    private List<BalanceEntity> balances;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(BalanceEntity balanceEntity) {
        final AccountTypes type =
                Xs2aDevelopersConstants.ACCOUNT_TYPE_MAPPER
                        .translate(accountType)
                        .orElse(AccountTypes.OTHER);

        switch (type) {
            case CHECKING:
                return Optional.ofNullable(toCheckingAccount(balanceEntity));
            case SAVINGS:
                return Optional.ofNullable(toSavingsAccount(balanceEntity));
            default:
                return Optional.empty();
        }
    }

    @JsonIgnore
    private TransactionalAccount toCheckingAccount(BalanceEntity balanceEntity) {

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumber())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                                .build())
                .withBalance(BalanceModule.of(balanceEntity.toAmount()))
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(getAccountNumber())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    @JsonIgnore
    private TransactionalAccount toSavingsAccount(BalanceEntity balanceEntity) {

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumber())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                                .build())
                .withBalance(BalanceModule.of(balanceEntity.toAmount()))
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(getAccountNumber())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    private String getAccountNumber() {
        return iban;
    }
}
