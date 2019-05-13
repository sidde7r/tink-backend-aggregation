package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

// import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants;

@JsonObject
public class AccountBaseEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String name;
    private String cashAccountType;

    private List<BalanceBaseEntity> balances;

    @JsonProperty("_links")
    private AccountLinksEntity links;

    public boolean isCheckingOrSavingsType() {
        return BerlinGroupConstants.ACCOUNT_TYPE_MAPPER.translate(cashAccountType).isPresent();
    }

    public TransactionalAccount toTinkAccount() {
        return BerlinGroupConstants.ACCOUNT_TYPE_MAPPER
                .translate(cashAccountType)
                .filter(this::isCheckingType)
                .map(accountType -> toCheckingAccount())
                .orElse(toSavingsAccount());
    }

    private boolean isCheckingType(AccountTypes accountType) {
        return accountType == AccountTypes.CHECKING;
    }

    private TransactionalAccount toCheckingAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(getBalance())
                .setAlias(name)
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .putInTemporaryStorage(StorageKeys.TRANSACTIONS_URL, getTransactionLink())
                .build();
    }

    private TransactionalAccount toSavingsAccount() {
        return SavingsAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(getBalance())
                .setAlias(name)
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .putInTemporaryStorage(
                        StorageKeys.TRANSACTIONS_URL, getTransactionLink()) // TODO: check if needed
                .build();
    }

    private Amount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(this::doesMatchWithAccountCurrency)
                .findFirst()
                .map(BalanceBaseEntity::toAmount)
                .orElse(getDefaultAmount());
    }

    private boolean doesMatchWithAccountCurrency(BalanceBaseEntity balance) {
        return balance.isClosingBooked() && balance.isInCurrency(currency);
    }

    private Amount getDefaultAmount() {
        return new Amount(currency, 0);
    }

    public String getIban() {
        return iban;
    }

    public String getTransactionLink() {
        return Optional.ofNullable(links).map(AccountLinksEntity::getTransactions).orElse("");
    }

    public String getBalancesLink() {
        return Optional.ofNullable(links).map(AccountLinksEntity::getBalances).orElse("");
    }

    public List<BalanceBaseEntity> getBalances() {
        return balances;
    }

    public void setBalances(List<BalanceBaseEntity> balances) {
        this.balances = balances;
    }
}
