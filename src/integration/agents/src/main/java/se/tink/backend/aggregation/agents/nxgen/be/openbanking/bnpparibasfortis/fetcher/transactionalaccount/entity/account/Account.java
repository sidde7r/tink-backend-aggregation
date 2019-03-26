
package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.balance.Balance;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class Account {

    @JsonProperty("_links")
    private Links links;
    private String bicFi;
    private String cashAccountType;
    private String currency;
    private String name;
    private String psuStatus;
    private String resourceId;
    private String usage;

    public TransactionalAccount toTinkModel(List<Balance> balances) {
        return CheckingAccount
            .builder()
            .setUniqueIdentifier(resourceId)
            .setAccountNumber(resourceId)
            .setBalance(getBalance(balances))
            .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, resourceId))
            .addHolderName(name)
            .setAlias(name)
            .setApiIdentifier(resourceId)
            .putInTemporaryStorage(BnpParibasFortisConstants.StorageKeys.ACCOUNT_LINKS, links)
            .build();
    }

    private Amount getBalance(List<Balance> balances) {
        return Optional
            .ofNullable(balances)
            .map(balances1 -> balances
                .stream()
                .filter(balance -> balance
                    .getBalanceType()
                    .equalsIgnoreCase(BnpParibasFortisConstants.Accounts.OTHR_BALANCE_TYPE))
                .findFirst()
                .map(balance -> new Amount(
                    balance.getBalanceAmount().getCurrency(),
                    Double.parseDouble(balance.getBalanceAmount().getAmount())))
                .orElseGet(this::defaultAmount))
            .orElse(defaultAmount());
    }

    public Amount defaultAmount() {
        return new Amount(currency, 0);
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
