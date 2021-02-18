package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.errors.rpc;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
public class TppMessageEntity {
    private String category;
    private String code;
}
