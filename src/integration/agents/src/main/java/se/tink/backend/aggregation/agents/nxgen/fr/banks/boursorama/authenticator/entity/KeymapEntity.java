package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import java.util.Map;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class KeymapEntity {
    private Map<String, String> keys;
}
