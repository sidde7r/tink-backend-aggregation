package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class InitSessionResponse {
    private String publicB;
    private String salt;

    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;
}
