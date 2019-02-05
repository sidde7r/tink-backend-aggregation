package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValueEntity {
    @JsonProperty("descripcion")
    private String description;

    @JsonProperty("codigosError")
    private ErrorCodeEntity errorCodes;

    @JsonProperty("contrato")
    private ContractEntity contract;

    @JsonProperty("nombreTitular")
    private String holderName;

    @JsonProperty("indicadorAcceso")
    private String indicadorAcceso;

    @JsonProperty("catalogData")
    private CatalogDataEntity catalogData;

    @JsonProperty("cuenta")
    private AccountValuesEntity accountValues;

    @JsonProperty("tipoCuenta")
    private String accountType;

    @JsonProperty("filtros")
    private FilterEntity filter;

    public String getDescription() {
        return description;
    }

    public ErrorCodeEntity getErrorCodes() {
        return errorCodes;
    }

    public ContractEntity getContract() {
        return contract;
    }

    public String getHolderName() {
        return holderName;
    }

    public String getIndicadorAcceso() {
        return indicadorAcceso;
    }

    public CatalogDataEntity getCatalogData() {
        return catalogData;
    }

    public AccountValuesEntity getAccountValues() {
        return accountValues;
    }

    public String getAccountType() {
        return accountType;
    }

    public FilterEntity getFilter() {
        return filter;
    }
}
