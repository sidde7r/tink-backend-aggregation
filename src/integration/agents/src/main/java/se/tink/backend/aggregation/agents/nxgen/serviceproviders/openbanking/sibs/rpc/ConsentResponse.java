package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentResponse {

    @JsonProperty("_links")
    private ConsentLinksEntity links;

    private String consentId;
}
