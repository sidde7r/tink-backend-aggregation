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

    public Optional<TransactionalAccount> toTinkAccount(
            BalancesItemEntity balancesItemEntity, AccountDetailsEntity accountDetails) {
        balancesItemEntity.setCurrencyIfNull(currency);
        ExactCurrencyAmount balance = balancesItemEntity.getBalanceAmount();

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        BecConstants.ACCOUNT_TYPE_MAPPER,
                        Optional.ofNullable(cashAccountType).orElse(product),
                        TransactionalAccountType.OTHER)
                .withBalance(BalanceModule.of(balance))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(Optional.ofNullable(name).orElse(product))
                                .addIdentifier(getIdentifier())
                                .build())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .addHolderName(Optional.ofNullable(accountDetails.getOwnerName()).orElse(""))
                .putInTemporaryStorage(BecConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    private String getUniqueIdentifier() {
        return bban;
    }

    private String getAccountNumber() {
        return bban;
    }

    private AccountIdentifier getIdentifier() {
        return new IbanIdentifier(iban);
    }

    public String getResourceId() {
        return resourceId;
    }
}
