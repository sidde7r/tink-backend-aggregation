package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ScaLink {
    private String href;
}
