package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.entity;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class ArgentaAccount {

    private String id;
    private String iban;
    private String alias;
    private String type;
    private double balance;
    private String currency;

    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(getAccountType(), iban, getBalance())
                .setBankIdentifier(id)
                .setAccountNumber(iban)
                .setName(alias)
                .build();
    }

    private Amount getBalance() {
        return currency != null ? new Amount(currency, balance) : Amount.inEUR(balance);
    }

    private AccountTypes getAccountType() {
        return ArgentaConstants.ACCOUNT_TYPE_MAPPER.translate(type).orElse(AccountTypes.OTHER);
    }
}
