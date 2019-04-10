package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
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
        return CheckingAccount.builder()
                .setUniqueIdentifier(resourceId)
                .setAccountNumber(resourceId)
                .setBalance(getBalance(balances))
                .setAlias(name)
                .addAccountIdentifier(
                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, resourceId))
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .putInTemporaryStorage(BnpParibasFortisConstants.StorageKeys.ACCOUNT_LINKS, links)
                .build();
    }

    private Amount getBalance(List<Balance> balances) {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(Balance::isBalanceTypeOther)
                .findFirst()
                .map(Balance::toTinkAmount)
                .orElse(new Amount(currency, 0));
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
