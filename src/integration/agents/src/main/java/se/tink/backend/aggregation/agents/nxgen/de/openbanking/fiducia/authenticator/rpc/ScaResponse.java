package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.ChallengeData;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.ScaMethod;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ScaResponse {
    @JsonProperty("_links")
    private LinksEntity links;

    private String scaStatus;
    private String authorisationId;
    private List<ScaMethod> scaMethods;
    @Setter private ScaMethod chosenScaMethod;
    private ChallengeData challengeData;
}
