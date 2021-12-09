package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountHoldersQueryEntity {

    @JsonProperty("codigoRetorno")
    private String resultCode;

    @JsonProperty("Respuesta")
    private AccountHoldersEntity accountHoldersEntity;

    public String getResultCode() {
        return resultCode;
    }

    public AccountHoldersEntity getAccountHoldersEntity() {
        return accountHoldersEntity;
    }
}
