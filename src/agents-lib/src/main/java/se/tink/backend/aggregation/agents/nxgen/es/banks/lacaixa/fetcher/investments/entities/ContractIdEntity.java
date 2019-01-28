package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractIdEntity {
    private String area;
    @JsonProperty("numeroContrato")
    private String contractNumber;
    private String empresa;
    private String oficina;
    private String longitudModalidad;
    private String modalidad;
    private String longitudCuenta;
    private String numeroCuenta;
    private String dcCuenta;

    public String getContractNumber() {
        return contractNumber;
    }
}
