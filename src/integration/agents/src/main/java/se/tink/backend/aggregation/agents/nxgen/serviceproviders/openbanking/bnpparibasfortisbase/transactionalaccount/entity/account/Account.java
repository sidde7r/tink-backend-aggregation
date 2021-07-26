package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.balance.Balance;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class Account {

    @JsonProperty("_links")
    private Links links;

    private String cashAccountType;
    private String product;
    private String currency;
    private String name;
    private String resourceId;

    public Optional<TransactionalAccount> toTinkModel(List<Balance> balances) {
        String iban = getIban();
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(getBalanceModule(balances))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(product)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_LINKS, links)
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .build();
    }

    private BalanceModule getBalanceModule(List<Balance> balances) {
        BalanceBuilderStep builder =
                BalanceModule.builder().withBalance(getBookedBalance(balances));
        getAvailableBalance(balances).ifPresent(builder::setAvailableBalance);

        return builder.build();
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance(List<Balance> balances) {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(Balance::isBalanceAvailable)
                .findFirst()
                .map(Balance::toTinkAmount);
    }

    private ExactCurrencyAmount getBookedBalance(List<Balance> balances) {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(Balance::isBalanceTypeOther)
                .findFirst()
                .map(Balance::toTinkAmount)
                .orElse(new ExactCurrencyAmount(BigDecimal.ZERO, currency));
    }

    private String getIban() {
        return resourceId.substring(0, resourceId.length() - 3);
    }

    public Links getLinks() {
        return links;
    }

    public String getCurrency() {
        return currency;
    }

    public String getName() {
        return name;
    }

    public String getCashAccountType() {
        return cashAccountType;
    }
}
