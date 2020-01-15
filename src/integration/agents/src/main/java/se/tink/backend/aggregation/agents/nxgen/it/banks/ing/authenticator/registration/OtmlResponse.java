package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Accessors(chain = true)
@JsonObject
public class OtmlResponse {
    private String datasources;
    private String target;
    private String xml;
}
