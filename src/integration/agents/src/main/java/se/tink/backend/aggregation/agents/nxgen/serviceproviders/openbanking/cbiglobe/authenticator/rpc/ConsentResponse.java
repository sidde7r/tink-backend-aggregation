package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ConsentResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private String consentId;

    private String consentStatus;

    private PsuCredentialsResponse psuCredentials;

    public ConsentResponse(LinksEntity links, String consentId, String consentStatus) {
        this(links, consentId, consentStatus, null);
    }

    public ConsentStatus getConsentStatus() {
        return ConsentStatus.valueOf(consentStatus.toUpperCase());
    }

    public URL getScaUrl() {
        String url = links.getAuthorizeUrl().getHref();

        return new URL(CbiGlobeUtils.encodeBlankSpaces(url));
    }
}
