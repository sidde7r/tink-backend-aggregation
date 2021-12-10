package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AuthorizationResponse {

    private static final String PSU_AUTHENTICATED = "psuAuthenticated";
    private static final String SCA_METHOD_SELECTED = "scaMethodSelected";

    @JsonProperty("_links")
    private LinksEntity links;

    private String scaStatus;
    private String authorisationId;
    private List<ScaMethodEntity> scaMethods;
    @Setter private ScaMethodEntity chosenScaMethod;
    private ChallengeDataEntity challengeData;
    private String psuMessage;

    public boolean isStatePsuAuthenticated() {
        return PSU_AUTHENTICATED.equalsIgnoreCase(scaStatus);
    }

    public boolean isStateScaMethodSelected() {
        return SCA_METHOD_SELECTED.equalsIgnoreCase(scaStatus);
    }
}
