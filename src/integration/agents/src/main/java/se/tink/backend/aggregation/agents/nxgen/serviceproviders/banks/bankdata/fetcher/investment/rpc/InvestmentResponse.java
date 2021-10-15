package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.investment.entities.InvestmentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class InvestmentResponse {
    @JsonProperty("data")
    private InvestmentEntity investmentEntity;
}
