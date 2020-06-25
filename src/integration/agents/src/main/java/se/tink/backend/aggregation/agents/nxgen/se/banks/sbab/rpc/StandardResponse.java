package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StandardResponse {
    private List<LinksEntity> links;

    public List<LinksEntity> getLinks() {
        return Optional.ofNullable(links).orElse(Collections.emptyList());
    }
}
