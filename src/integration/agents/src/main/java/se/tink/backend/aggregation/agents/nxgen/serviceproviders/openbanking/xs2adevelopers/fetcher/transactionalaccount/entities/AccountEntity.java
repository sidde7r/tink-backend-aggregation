package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Strings;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private String accountType;
    private String bban;
    private String bic;
    private String cashAccountType;
    private String currency;
    private String iban;
    private String id;
    private String maskedPan;
    private String msisdn;
    private String name;
    private String resourceId;
    private List<BalanceEntity> balances;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {
        final AccountTypes type =
                Xs2aDevelopersConstants.ACCOUNT_TYPE_MAPPER
                        .translate(accountType)
                        .orElse(AccountTypes.OTHER);

        switch (type) {
            case CHECKING:
                return Optional.ofNullable(toTypeAccount(TransactionalAccountType.CHECKING));
            case SAVINGS:
                return Optional.ofNullable(toTypeAccount(TransactionalAccountType.SAVINGS));
            case OTHER:
            default:
                return Optional.empty();
        }
    }

    @JsonIgnore
    private TransactionalAccount toTypeAccount(TransactionalAccountType transactionalAccountType) {

        String accountName;
        if (!Strings.isNullOrEmpty(name)) {
            accountName = name;
        } else {
            accountName = iban;
        }

        return TransactionalAccount.nxBuilder()
                .withType(transactionalAccountType)
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumber())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(accountName)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                                .build())
                .addHolderName(accountName)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(getAccountNumber())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    @JsonIgnore
    private Amount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailableBalance)
                .map(BalanceEntity::toAmount)
                .findFirst()
                .orElse(BalanceEntity.DEFAULT);
    }

    public String getResourceId() {
        return resourceId;
    }

    private String getAccountNumber() {
        return iban;
    }

    public void setBalance(List<BalanceEntity> balances) {
        this.balances = balances;
    }
}
