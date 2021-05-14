package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.entities.ScaEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ScaResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    public Optional<String> getRedirectUri() {
        return Optional.ofNullable(links).map(LinksEntity::getScaRedirect).map(ScaEntity::getHref);
    }
}
