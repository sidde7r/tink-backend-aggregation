package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetConsentResponse {
    @JsonProperty private String psuMessage;
    @JsonProperty private List<TppMessageEntity> tppMessages;
    @JsonProperty private String consentStatus;
    @JsonProperty private String consentId;

    @JsonProperty("_links")
    private Map<String, LinkEntity> links;

    @JsonIgnore
    public String getConsentId() {
        return consentId;
    }

    @JsonIgnore
    public Map<String, LinkEntity> getLinks() {
        return links;
    }

    @JsonIgnore
    public Optional<LinkEntity> getLink(String linkName) {
        if (links == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(links.get(linkName));
    }
}
