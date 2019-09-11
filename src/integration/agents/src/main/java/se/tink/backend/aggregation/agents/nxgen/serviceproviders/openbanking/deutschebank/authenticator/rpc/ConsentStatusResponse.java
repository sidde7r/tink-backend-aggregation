package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc;

<<<<<<< HEAD
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
=======
>>>>>>> 6d66ab014d6ce0c10e31a52e2682c7d21def5624
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentStatusResponse {
<<<<<<< HEAD
    @JsonProperty private String consentStatus;
=======

    @JsonProperty
    private String consentStatus;
>>>>>>> 6d66ab014d6ce0c10e31a52e2682c7d21def5624

    @JsonIgnore
    public String getConsentStatus() {
        return consentStatus;
    }
}
