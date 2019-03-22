package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankOperationEntity {
    @JsonProperty("OPERACION_BASICA")
    private String basicOperation;

    @JsonProperty("OPERACION_BANCARIA")
    private String bankingOperation;
}
