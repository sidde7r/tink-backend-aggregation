package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.MortgageInstallmentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MortgageInstallmentsResponse {

    private List<MortgageInstallmentEntity> installments;

    @JsonProperty("lastPage")
    private Boolean isLastPage;

    @JsonProperty("pageKey")
    private String nextPageKey;

    public List<MortgageInstallmentEntity> getInstallments() {
        return installments;
    }

    public Boolean getIsLastPage() {
        return isLastPage;
    }

    public String getNextPageKey() {
        return nextPageKey;
    }
}
