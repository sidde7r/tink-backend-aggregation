package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponseWithoutHref;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse extends ConsentBaseResponseWithoutHref {
    private String authorisationId;

    public String getAuthorisationId() {
        return authorisationId;
    }
}
