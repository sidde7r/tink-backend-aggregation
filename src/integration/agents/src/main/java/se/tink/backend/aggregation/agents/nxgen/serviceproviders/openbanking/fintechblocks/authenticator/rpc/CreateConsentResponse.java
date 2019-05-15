package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.MetaEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.ResponseDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.RiskEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateConsentResponse {

    @JsonProperty("Data")
    private ResponseDataEntity data;

    @JsonProperty("Links")
    private LinksEntity links;

    @JsonProperty("Meta")
    private MetaEntity meta;

    @JsonProperty("Risk")
    private RiskEntity risk;

    public ResponseDataEntity getData() {
        return data;
    }
}
