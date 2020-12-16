package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.fetcher.pension.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.fetcher.pension.entities.GeneralPensionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class PensionAccountsResponse {
    @JsonProperty("ap")
    private GeneralPensionEntity generalPension;

    private boolean accountEmploymentInfoIsMissing;
    private String fodelsedatum;
}
