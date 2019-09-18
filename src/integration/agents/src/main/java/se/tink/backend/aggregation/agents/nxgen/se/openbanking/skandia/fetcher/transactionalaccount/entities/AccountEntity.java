package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants;
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

    private String bban;
    private String bic;
    private String cashAccountType;
    private String currency;
    private String iban;
    private String resourceId;
    private String usage;

    @JsonProperty("name")
    private String productName;

    private List<BalanceEntity> balances;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        SkandiaConstants.ACCOUNT_TYPE_MAPPER,
                        productName,
                        TransactionalAccountType.OTHER)
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueId())
                                .withAccountNumber(iban)
                                .withAccountName(Optional.ofNullable(productName).orElse(""))
                                .addIdentifier(getIdentifier())
                                .build())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .build();
    }

    private AccountIdentifier getIdentifier() {
        return new IbanIdentifier(iban);
    }

    private ExactCurrencyAmount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .orElseThrow(() -> new IllegalStateException("No balance found"));
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getUniqueId() {
        String clearingNumber = iban.substring(4, 8);
        return clearingNumber + bban + "-" + clearingNumber + bban;
    }

    public void setBalances(List<BalanceEntity> balances) {
        this.balances = balances;
    }
}
