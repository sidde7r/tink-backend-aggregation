package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String iban;
    private String currency;
    private String name;
    private String cashAccountType;

    private List<BalanceBaseEntity> balances;

    @JsonProperty("_links")
    private AccountLinksEntity links;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        BerlinGroupConstants.ACCOUNT_TYPE_MAPPER,
                        cashAccountType,
                        TransactionalAccountType.OTHER)
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(name)
                                .addIdentifier(
                                        new IbanIdentifier(iban.substring(iban.length() - 18)))
                                .build())
                .putInTemporaryStorage(SantanderConstants.StorageKeys.ACCOUNT_ID, iban)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .putInTemporaryStorage(
                        SantanderConstants.StorageKeys.TRANSACTIONS_URL, getTransactionLink())
                .build();
    }

    private ExactCurrencyAmount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(this::doesMatchWithAccountCurrency)
                .findFirst()
                .map(BalanceBaseEntity::toTinkAmount)
                .orElse(getDefaultAmount());
    }

    private boolean doesMatchWithAccountCurrency(final BalanceBaseEntity balance) {
        return balance.isClosingBooked() && balance.isInCurrency(currency);
    }

    private ExactCurrencyAmount getDefaultAmount() {
        return ExactCurrencyAmount.zero(currency);
    }

    private String getTransactionLink() {
        return Optional.ofNullable(links).map(AccountLinksEntity::getTransactionLink).orElse("");
    }
}
