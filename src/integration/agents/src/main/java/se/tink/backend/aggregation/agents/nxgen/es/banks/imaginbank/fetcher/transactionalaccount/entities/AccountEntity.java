package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private String alias;

    @JsonProperty("saldoDisponible")
    private double availableBalance;

    @JsonProperty("moneda")
    private String currency;

    @JsonProperty("numeroCuenta")
    private AccountIdentifierEntity identifiers;

    @JsonIgnore
    public TransactionalAccount toTinkAccount(HolderName holderName) {
        // Imagin Bank only allows one account, a checking account
        // the api client logs if we receive more than one account because that would imply a major
        // change
        return TransactionalAccount.builder(
                        AccountTypes.CHECKING,
                        identifiers.getIban(),
                        new Amount(currency, availableBalance))
                .setAccountNumber(identifiers.getIban())
                .setName(alias)
                .addIdentifiers(identifiers.getIdentifiers())
                .putInTemporaryStorage(
                        ImaginBankConstants.TemporaryStorage.ACCOUNT_REFERENCE,
                        identifiers.getAccountReference())
                .setHolderName(holderName)
                .build();
    }
}
