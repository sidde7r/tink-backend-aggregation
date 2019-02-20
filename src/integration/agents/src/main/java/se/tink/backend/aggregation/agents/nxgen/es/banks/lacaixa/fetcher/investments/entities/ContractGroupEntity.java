package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractGroupEntity {
    private String literalAgrupacion;
    @JsonProperty("codigoAgrupacion")
    private String contractGroupCode;
    private boolean agrupacionCerrada;
    @JsonProperty("contrato")
    private List<ContractEntity> contracts;

    @JsonIgnore
    public Map<String, String> getProductCodeByContractNumber() {
        return Optional.ofNullable(contracts).orElse(Collections.emptyList()).stream()
                .collect(Collectors.toMap(ContractEntity::getContractNumber, ContractEntity::getProductCode));
    }

    public String getContractGroupCode() {
        return contractGroupCode;
    }

    public List<ContractEntity> getContracts() {
        return contracts;
    }
}
