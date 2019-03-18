package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.LoanOverviewEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanOverviewResponse {
    @JsonProperty("resultadoCorrecto")
    private boolean resultOk;
    @JsonProperty("resultadoMensaje")
    private boolean resultMessage;
    @JsonProperty("productos")
    private List<LoanOverviewEntity> products;

    public boolean isResultOk() {
        return resultOk;
    }

    public List<LoanOverviewEntity> getProducts() {
        return products;
    }
}
