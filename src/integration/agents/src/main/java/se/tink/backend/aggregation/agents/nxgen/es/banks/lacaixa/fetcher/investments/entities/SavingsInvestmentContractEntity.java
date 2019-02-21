package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingsInvestmentContractEntity {
    @JsonProperty("contratosAgrupados")
    private List<ContractGroupEntity> contractGroups;
    private boolean masDatos;

    @JsonIgnore
    public Map<String, String> getProductCodeByContractNumber() {
        Map<String, String> response = new HashMap<>();

        Optional.ofNullable(contractGroups).orElse(Collections.emptyList()).stream()
                .forEach(contractGroup -> response.putAll(contractGroup.getProductCodeByContractNumber()));

        return response;
    }

    @JsonIgnore
    public Optional<String> getContractName(String contractNumber) {
        return Optional.ofNullable(contractGroups).orElse(Collections.emptyList()).stream()
                .map(ContractGroupEntity::getContracts)
                .flatMap(List::stream)
                .filter(contract -> contractNumber.equalsIgnoreCase(contract.getContractNumberFormatted()))
                .map(ContractEntity::getContractAlias)
                .findFirst();
    }
}
