package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaAuthorizationLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public class InitAuthorizationResponse {

    @JsonProperty("_links")
    private ScaAuthorizationLinksEntity links;

    private String scaStatus;
    private String authorisationId;
    private List<ScaMethodEntity> scaMethods;
    private ChallengeDataEntity challengeData;
    private String psuMessage;

    public List<ScaMethodEntity> getScaMethods() {
        return Optional.ofNullable(scaMethods).orElse(Collections.emptyList());
    }

    public String getAuthorisationId() {
        return authorisationId;
    }

    public ChallengeDataEntity getChallengeData() {
        return challengeData;
    }

    public String getScaStatus() {
        return links.getScaStatus().getHref();
    }

    public URL getAuthorizationUrl() {
        return new URL(
                Optional.ofNullable(links)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.MISSING_SCA_AUTHORIZATION_URL))
                        .getAuthoriseTransaction()
                        .getHref());
    }
}
