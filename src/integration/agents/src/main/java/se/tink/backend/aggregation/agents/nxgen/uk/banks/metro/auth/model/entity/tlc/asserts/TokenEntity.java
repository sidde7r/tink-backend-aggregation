package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class TokenEntity {
    private String token;
}
