package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountLinksWithHrefEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BerlinGroupAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class ErsteAccountEntity implements BerlinGroupAccountEntity {

    private String resourceId;
    private String iban;
    private String currency;
    private String name;
    private String cashAccountType;

    private List<BalanceBaseEntity> balances;

    @JsonProperty("_links")
    private AccountLinksWithHrefEntity links;

    @Override
    public Optional<TransactionalAccount> toTinkAccount() {
        return BerlinGroupConstants.ACCOUNT_TYPE_MAPPER
                .translate(cashAccountType)
                .flatMap(this::toTransactionalAccount);
    }

    private Optional<TransactionalAccount> toTransactionalAccount(TransactionalAccountType type) {
        return TransactionalAccount.nxBuilder()
                .withType(type)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(cashAccountType)
                                .addIdentifier(getIdentifier())
                                .build())
                .putInTemporaryStorage(StorageKeys.TRANSACTIONS_URL, getTransactionLink())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(getUniqueIdentifier())
                .addHolderName(name)
                .build();
    }

    @Override
    public ExactCurrencyAmount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(this::doesMatchWithAccountCurrency)
                .findFirst()
                .map(BalanceBaseEntity::toAmount)
                .orElse(getDefaultAmount());
    }

    @Override
    public boolean doesMatchWithAccountCurrency(BalanceBaseEntity balance) {
        return balance.isClosingBooked() && balance.isInCurrency(currency);
    }

    @Override
    public ExactCurrencyAmount getDefaultAmount() {
        return ExactCurrencyAmount.zero(currency);
    }

    @Override
    public String getBalancesLink() {
        return Optional.ofNullable(links)
                .map(AccountLinksWithHrefEntity::getBalanceLink)
                .orElse("");
    }

    @Override
    public String getTransactionLink() {
        return Optional.ofNullable(links)
                .map(AccountLinksWithHrefEntity::getTransactionLink)
                .orElse("");
    }

    @Override
    public String getUniqueIdentifier() {
        return iban;
    }

    @Override
    public AccountIdentifier getIdentifier() {
        return new IbanIdentifier(iban);
    }

    @Override
    public String getAccountNumber() {
        return getUniqueIdentifier();
    }
}
