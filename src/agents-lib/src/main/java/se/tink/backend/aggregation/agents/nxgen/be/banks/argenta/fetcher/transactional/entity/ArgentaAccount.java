package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;

import java.util.Optional;

@JsonObject
public class ArgentaAccount {
    String id;
    String iban;
    String alias;
    String type;
    double balance;
    String currency;

    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(getAccountType(), iban, getBalance())
                .setBankIdentifier(id)
                .setAccountNumber(iban)
                .setName(alias)
                .build();
    }

    private Amount getBalance() {
        if (currency != null) return new Amount(currency, balance);
        return Amount.inEUR(balance);
    }

    private AccountTypes getAccountType() {
        Optional<AccountTypes> accountType = ArgentaConstants.ACCOUNT_TYPE_MAPPER.translate(type);
        return accountType.orElse(AccountTypes.OTHER);
    }
}
