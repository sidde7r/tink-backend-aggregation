package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.entities.ConsentLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentResponse {

    private String consentStatus;
    private String consentId;
    private AccessEntity access;
    private boolean recurringIndicator;
    private String validUntil;
    private int frequencyPerDay;

    @JsonProperty("_links")
    private ConsentLinksEntity links;
}
