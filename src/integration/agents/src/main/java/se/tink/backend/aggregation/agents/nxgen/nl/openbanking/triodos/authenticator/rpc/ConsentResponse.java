package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponseWithoutHref;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse extends ConsentBaseResponseWithoutHref {

    @Getter private String authorisationId;
}
