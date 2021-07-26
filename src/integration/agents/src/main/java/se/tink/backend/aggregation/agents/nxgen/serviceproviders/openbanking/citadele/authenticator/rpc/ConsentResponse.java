package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.entities.ConsentBaseLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentResponse {

    private String consentStatus;
    private String consentId;
    private String scaMethods;
    private String chosenScaMethod;
    private String challengeData;

    @JsonProperty("_links")
    private ConsentBaseLinksEntity links;
}
