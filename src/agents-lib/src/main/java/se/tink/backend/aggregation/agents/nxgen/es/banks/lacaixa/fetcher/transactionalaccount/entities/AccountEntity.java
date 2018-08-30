package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class AccountEntity {

    private String alias;

    @JsonProperty("tipoCuenta")
    private String accountType;

    @JsonProperty("saldo")
    private BalanceEntity balance;

    @JsonProperty("numeroCuenta")
    private AccountIdentifierEntity identifiers;


    @JsonIgnore
    public TransactionalAccount toTinkAccount(HolderName holderName) {
        return TransactionalAccount.builder(AccountTypes.CHECKING, identifiers.getIban(), balance)
                .setAccountNumber(identifiers.getIban())
                .setName(alias)
                .addIdentifiers(identifiers.getIdentifiers())
                .putInTemporaryStorage(LaCaixaConstants.TemporaryStorage.ACCOUNT_REFERENCE, identifiers.getAccountReference())
                .setHolderName(holderName)
                .build();
    }
}
