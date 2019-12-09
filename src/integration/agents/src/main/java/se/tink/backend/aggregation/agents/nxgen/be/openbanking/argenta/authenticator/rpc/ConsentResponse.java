package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String consentId;
    private String consentStatus;
    private List<ScaMethodEntity> scaMethods;

    @JsonProperty("_links")
    private LinksEntity links;

    public String getConsentId() {
        return consentId;
    }

    public List<ScaMethodEntity> getScaMethods() {
        return Optional.ofNullable(scaMethods).orElse(Collections.emptyList());
    }

    public String getSelectAuthenticationMethodUrl() {
        Preconditions.checkNotNull(links);
        return Preconditions.checkNotNull(links.getSelectAuthenticationMethod()).getHref();
    }
}
