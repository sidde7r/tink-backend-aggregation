package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardEntity {
    @JsonProperty("contrato")
    private ContractEntity contract;
    @JsonProperty("saldoInformado")
    private boolean informedBalance;
    @JsonProperty("saldoDisponible")
    private AmountEntity availableBalance;
    @JsonProperty("limiteCredito")
    private AmountEntity creditLimit;
}
