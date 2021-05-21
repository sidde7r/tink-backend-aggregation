package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.investment.entities.InvestmentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class InvestmentResponse {
    @JsonProperty("data")
    private InvestmentEntity investmentEntity;
}
