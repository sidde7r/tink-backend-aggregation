package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferResponse extends AbstractChallengeResponse {
    @JsonProperty("Details")
    private List<DetailsEntity> details;

    public List<DetailsEntity> getDetails() {
        return details;
    }
}
