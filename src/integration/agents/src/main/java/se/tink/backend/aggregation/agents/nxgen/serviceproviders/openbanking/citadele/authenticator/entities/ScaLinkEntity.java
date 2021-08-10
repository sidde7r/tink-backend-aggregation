package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ScaLinkEntity {
    private String href;
}
