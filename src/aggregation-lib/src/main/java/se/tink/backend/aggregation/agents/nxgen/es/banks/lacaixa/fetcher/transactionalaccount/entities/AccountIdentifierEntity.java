package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.Collection;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class AccountIdentifierEntity {

    private String iban;

    @JsonProperty("numeroCuenta")
    private String accountNumber;

    public String getIban() {
        return iban;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Collection<AccountIdentifier> getIdentifiers(){

        return Arrays.asList(new IbanIdentifier(iban));
    }
}
