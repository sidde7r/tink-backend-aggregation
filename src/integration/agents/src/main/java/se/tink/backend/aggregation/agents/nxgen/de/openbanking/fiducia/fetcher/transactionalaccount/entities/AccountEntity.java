package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private String currency;
    private String iban;
    private String resourceId;
    private List<BalanceEntity> balances;

    public String getResourceId() {
        return resourceId;
    }

    public void setBalances(List<BalanceEntity> balances) {
        this.balances = balances;
    }

    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(getAvailableBalance())
                .setAlias(iban)
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .setApiIdentifier(resourceId)
                .build();
    }

    private Amount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailableBalance)
                .map(BalanceEntity::getAmount)
                .findFirst()
                .orElse(BalanceEntity.Default);
    }
}
