package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetConsentResponse {
    @JsonProperty private String psuMessage;
    @JsonProperty private List<TppMessageEntity> tppMessages;
    @JsonProperty private RedsysConstants.ConsentStatus consentStatus;
    @JsonProperty private String consentId;

    @JsonProperty("_links")
    private Map<String, LinkEntity> links;

    public String getConsentId() {
        return consentId;
    }

    public Map<String, LinkEntity> getLinks() {
        return links;
    }

    @JsonIgnore
    public Optional<LinkEntity> getLink(String linkName) {
        return Optional.ofNullable(links.get(linkName));
    }
}
