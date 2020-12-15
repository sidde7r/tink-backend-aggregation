package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.entities.TppMessagesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationResponse {
    @JsonProperty("_links")
    private LinksEntity links;

    private String scaStatus;
    private ChallengeDataEntity challengeData;
    private List<TppMessagesEntity> tppMessages;

    public String getCollectAuthUri() {
        return links.getScaStatus().getHref();
    }

    public String getScaStatus() {
        return scaStatus;
    }

    public ChallengeDataEntity getChallengeData() {
        return challengeData;
    }

    public boolean isInterrupted() {
        return ListUtils.emptyIfNull(tppMessages).stream()
                .anyMatch(
                        tppMessage ->
                                ErrorCodes.USER_INTERUPTION.equalsIgnoreCase(tppMessage.getCode()));
    }
}
