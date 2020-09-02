package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class StandardResponse {
    private List<LinksEntity> links = Collections.emptyList();
}
