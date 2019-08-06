package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.entity;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;

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
        return ArgentaConstants.ACCOUNT_TYPE_MAPPER.translate(type).orElse(AccountTypes.OTHER);
    }
}
