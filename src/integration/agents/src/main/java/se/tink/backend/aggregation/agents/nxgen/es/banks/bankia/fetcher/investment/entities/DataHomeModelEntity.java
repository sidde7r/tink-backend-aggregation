package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DataHomeModelEntity {

    @JsonProperty("identificadorCuentaValores")
    private ValueAccountIdentifierEntity valueAccountIdentifier;

    @JsonProperty("datosRellamadaEntrada")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String dataRedialEntry;

    public DataHomeModelEntity(
            ValueAccountIdentifierEntity valueAccountIdentifier, String dataRedialEntry) {
        this.valueAccountIdentifier = valueAccountIdentifier;
        this.dataRedialEntry = dataRedialEntry;
    }

    public ValueAccountIdentifierEntity getValueAccountIdentifier() {
        return valueAccountIdentifier;
    }

    public String getDataRedialEntry() {
        return dataRedialEntry;
    }
}
