package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanOverviewResponse {
    @JsonProperty("resultadoCorrecto")
    private boolean resultOk;
    @JsonProperty("resultadoMensaje")
    private boolean resultMessage;
    @JsonProperty("productos")
    private List<Object> products;

    @JsonIgnore
    public boolean hasProducts() {
        return Optional.ofNullable(products)
                .map(p -> !p.isEmpty())
                .orElse(false);
    }
}
