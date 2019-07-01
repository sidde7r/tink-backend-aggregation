package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities.ScaMethodsItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizationResponse {

    @JsonProperty("consentId")
    private String consentId;

    @JsonProperty("scaMethods")
    private List<ScaMethodsItemEntity> scaMethods;

    @JsonProperty("consentStatus")
    private String consentStatus;

    public String getConsentId() {
        return consentId;
    }

    public List<ScaMethodsItemEntity> getScaMethods() {
        return scaMethods;
    }

    public String getConsentStatus() {
        return consentStatus;
    }
}
