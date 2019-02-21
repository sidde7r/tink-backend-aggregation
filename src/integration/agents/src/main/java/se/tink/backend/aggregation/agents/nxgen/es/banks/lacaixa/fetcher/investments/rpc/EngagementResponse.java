package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities.SavingsInvestmentContractEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EngagementResponse {
    private String prodSaldoCeroCaixaBank;
    private boolean hayAplicativoCerrado;
    @JsonProperty("listaContratosAhorroInversionCaixaBank")
    private SavingsInvestmentContractEntity savingsInvestmentContract;
    //private FinancingEntity listaContratosFinanciacionCaixaBank;

    @JsonIgnore
    public Map<String, String> getProductCodeByContractNumber() {
        return savingsInvestmentContract.getProductCodeByContractNumber();
    }

    @JsonIgnore
    public Optional<String> getContractName(String contractNumber) {
        return savingsInvestmentContract.getContractName(contractNumber);
    }
}
