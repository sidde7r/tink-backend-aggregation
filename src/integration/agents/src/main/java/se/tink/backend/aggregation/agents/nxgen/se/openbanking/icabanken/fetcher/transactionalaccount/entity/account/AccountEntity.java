package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.account;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private List<BalanceEntity> balances;
    private String bban;
    private String bic;
    private String currency;
    private String iban;
    private String name;
    private List<String> owner;
    private String resourceId;

    public TransactionalAccount toTinkModel() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(getBalance())
                .setAlias(name)
                .addAccountIdentifier(new IbanIdentifier(iban))
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .build();
    }

    private Amount getBalance() {
        return this.balances.stream()
                .filter(BalanceEntity::isInterimAvailable)
                .findFirst()
                .map(BalanceEntity::getAmount)
                .orElseGet(() -> new Amount(currency, 0));
    }
}
