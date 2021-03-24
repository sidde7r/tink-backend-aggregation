package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String name;
    private String bic;
    private String product;

    private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private LinksEntity links;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getBban())
                                .withAccountNumber(getBban())
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
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
                .map(BalanceEntity::toAmount)
                .orElse(getDefaultAmount());
    }

    private String getBban() {
        return iban.substring(iban.length() - 11);
    }

    private boolean isMatchWithAccountCurrency(BalanceEntity balance) {
        return balance.isClosingBooked() && balance.isInCurrency(currency);
    }

    private ExactCurrencyAmount getDefaultAmount() {
        return ExactCurrencyAmount.zero(currency);
    }

    public String getIban() {
        return iban;
    }

    public void setBalances(List<BalanceEntity> balances) {
        this.balances = balances;
    }

    public String getResourceId() {
        return resourceId;
    }
}
