package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountIdentifierEntity {

    private String iban;

    @JsonProperty("numeroCuenta")
    private String accountNumber;

    @JsonProperty("refvalCuenta")
    private String accountReference; // url used to fetch transactions

    public String getIban() {
        return iban;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountReference() {
        return accountReference;
    }
}
