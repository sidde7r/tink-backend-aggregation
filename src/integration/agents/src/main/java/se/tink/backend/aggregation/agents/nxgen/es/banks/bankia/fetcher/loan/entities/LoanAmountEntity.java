package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanAmountEntity {
    @JsonProperty("importeCapitalAmortizado")
    private AmountEntity amortizedAmount;
    @JsonProperty("importeCapitalConcedido")
    private AmountEntity grantedAmount;
    private AmountEntity importeCapitalPendienteNoVencido;
    private AmountEntity importeCapitalPendienteVencido;
    private AmountEntity importeDeudaVencida;

    public AmountEntity getAmortizedAmount() {
        return amortizedAmount;
    }

    public AmountEntity getGrantedAmount() {
        return grantedAmount;
    }
}
