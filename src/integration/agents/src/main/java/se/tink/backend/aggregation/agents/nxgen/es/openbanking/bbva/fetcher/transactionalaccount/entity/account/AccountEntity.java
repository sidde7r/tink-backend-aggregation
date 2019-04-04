package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    private String id;
    private String alias;
    private String type;
    private Boolean operable;
    private String description;
    private String number;
    private String currency;
    private double balance;
    private FormatsEntity formats;
    private LinksEntity links;
    private UserRoleEntity userRole;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public TransactionalAccount ToTinkAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(formats.getIban())
                .setAccountNumber(number)
                .setBalance(new Amount(currency, balance))
                .setAlias(alias)
                .addAccountIdentifier(new IbanIdentifier(formats.getIban()))
                .addAccountIdentifier(new TinkIdentifier(id))
                .setApiIdentifier(id)
                .build();
    }
}
