package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse extends ConsentBaseResponse {
    @JsonProperty("_links")
    public LinksEntity links;

    public LinksEntity getLinks() {
        return links;
    }
}
