package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
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
    private String resourceId;
    private String product;
    private String iban;
    private String currency;
    private String status;
    private String name;
    private String cashAccountType;
    private List<BalanceBaseEntity> balances;

    @JsonProperty("_links")
    private AccountLinksWithHrefEntity links;

    public String getResourceId() {
        return resourceId;
    }

    public Optional<TransactionalAccount> toTinkAccount(List<BalanceBaseEntity> balanceEntities) {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        DeutscheBankConstants.ACCOUNT_TYPE_MAPPER,
                        Optional.ofNullable(cashAccountType).orElse(product),
                        TransactionalAccountType.CHECKING)
                .withBalance(BalanceModule.of(getBalance(balanceEntities)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(getAccountName())
                                .addIdentifier(getIdentifier())
                                .build())
                .putInTemporaryStorage(StorageKeys.TRANSACTIONS_URL, getTransactionLink())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(getUniqueIdentifier())
                .build();
    }

    private ExactCurrencyAmount getBalance(List<BalanceBaseEntity> balanceEntities) {
        return Stream.of(balanceEntities, balances)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(this::doesMatchWithAccountCurrency)
                .findFirst()
                .map(BalanceBaseEntity::toAmount)
                .orElseThrow(() -> new IllegalStateException("Unable to fetch balance"));
    }

    private boolean doesMatchWithAccountCurrency(final BalanceBaseEntity balance) {
        return (balance.isClosingBooked() || balance.isExpected())
                && balance.isInCurrency(currency);
    }

    private String getTransactionLink() {
        return Optional.ofNullable(links)
                .map(AccountLinksWithHrefEntity::getTransactionLink)
                .orElse("");
    }

    private String getUniqueIdentifier() {
        return iban;
    }

    private AccountIdentifier getIdentifier() {
        return new IbanIdentifier(iban);
    }

    private String getAccountNumber() {
        return iban;
    }

    private String getAccountName() {
        return Stream.of(name, cashAccountType, product)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
