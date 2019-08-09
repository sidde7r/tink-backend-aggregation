package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String name;
    private String bic;
    private String product;

    private List<BalanceBaseEntity> balances;

    @JsonProperty("_links")
    private LinksEntity links;

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.from(AccountTypes.CHECKING))
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getBban())
                                .withAccountNumber(getBban())
                                .withAccountName(name)
                                .addIdentifier(AccountIdentifier.create(Type.IBAN, iban))
                                .build())
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(iban)
                .build();
    }

    private ExactCurrencyAmount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(this::isMatchWithAccountCurrency)
                .findFirst()
                .map(BalanceBaseEntity::toAmount)
                .orElse(getDefaultAmount());
    }

    private String getBban() {
        return iban.substring(iban.length() - 11);
    }

    private boolean isMatchWithAccountCurrency(BalanceBaseEntity balance) {
        return balance.isClosingBooked() && balance.isInCurrency(currency);
    }

    private ExactCurrencyAmount getDefaultAmount() {
        return ExactCurrencyAmount.of(BigDecimal.ZERO, currency);
    }

    public String getIban() {
        return iban;
    }

    public void setBalances(List<BalanceBaseEntity> balances) {
        this.balances = balances;
    }

    public String getResourceId() {
        return resourceId;
    }
}
