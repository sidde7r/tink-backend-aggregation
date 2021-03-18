package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountsItemEntity {

    @JsonProperty("cashAccountType")
    private String cashAccountType;

    @JsonProperty("balances")
    private List<BalancesItemEntity> balances;

    @JsonProperty("product")
    private String product;

    @JsonProperty("resourceId")
    private String resourceId;

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("name")
    private String name;

    @JsonProperty("currency")
    private String currency;

    public String getCashAccountType() {
        return cashAccountType;
    }

    public String getProduct() {
        return product;
    }

    public String getResourceId() {
        return resourceId;
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

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        KbcConstants.ACCOUNT_TYPE_MAPPER,
                        cashAccountType,
                        TransactionalAccountType.CHECKING)
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(product)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.SEPA_EUR, iban, name))
                                .build())
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .putInTemporaryStorage(KbcConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    private ExactCurrencyAmount getAvailableBalance() {

        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .findFirst()
                .map(BalancesItemEntity::getBalanceAmountEntity)
                .orElseThrow(() -> new IllegalStateException("Could not get balance"));
    }
}
