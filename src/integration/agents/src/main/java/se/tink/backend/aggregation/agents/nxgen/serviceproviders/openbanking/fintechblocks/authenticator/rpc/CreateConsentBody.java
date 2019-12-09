package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.RequestDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.RiskEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateConsentBody {

    @JsonProperty("Data")
    private RequestDataEntity data;

    @JsonProperty("Risk")
    private RiskEntity risk;

    public CreateConsentBody(RequestDataEntity data, RiskEntity risk) {
        this.data = data;
        this.risk = risk;
    }
}
