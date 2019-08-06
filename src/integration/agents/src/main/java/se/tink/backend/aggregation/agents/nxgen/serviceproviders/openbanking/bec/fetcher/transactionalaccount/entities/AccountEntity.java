package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private String bic;
    private String name;
    private String currency;
    private String iban;
    private String product;
    private String bban;
    private String resourceId;
    private String cashAccountType;
    private List<BalanceEntity> balances;

    public Optional<TransactionalAccount> toTinkAccount(BalancesItemEntity balancesItemEntity) {
        TransactionalAccountType type = getAccountType();
        balancesItemEntity.setCurrencyIfNull(currency);

        ExactCurrencyAmount balance = balancesItemEntity.getBalanceAmount();
        switch (type) {
            case SAVINGS:
                return toAccount(TransactionalAccountType.SAVINGS, balance);
            case CHECKING:
                return toAccount(TransactionalAccountType.CHECKING, balance);
            default:
                return Optional.empty();
        }
    }

    private Optional<TransactionalAccount> toAccount(
            TransactionalAccountType type, ExactCurrencyAmount balance) {
        return Optional.of(
                TransactionalAccount.nxBuilder()
                        .withType(type)
                        .withBalance(BalanceModule.of(balance))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(getUniqueIdentifier())
                                        .withAccountNumber(getAccountNumber())
                                        .withAccountName(Optional.ofNullable(name).orElse(product))
                                        .addIdentifier(getIdentifier())
                                        .addIdentifier(AccountIdentifier.create(Type.DK, bban))
                                        .build())
                        .setApiIdentifier(resourceId)
                        .setBankIdentifier(resourceId)
                        .addHolderName(Optional.ofNullable(name).orElse(""))
                        .putInTemporaryStorage(BecConstants.StorageKeys.ACCOUNT_ID, resourceId)
                        .build());
    }

    private String getUniqueIdentifier() {
        return iban;
    }

    private String getAccountNumber() {
        return iban;
    }

    private AccountIdentifier getIdentifier() {
        return new IbanIdentifier(iban);
    }

    private TransactionalAccountType getAccountType() {
        return BecConstants.ACCOUNT_TYPE_MAPPER
                .translate(Optional.ofNullable(cashAccountType).orElse(product))
                .orElse(TransactionalAccountType.OTHER);
    }

    public String getResourceId() {
        return resourceId;
    }
}
