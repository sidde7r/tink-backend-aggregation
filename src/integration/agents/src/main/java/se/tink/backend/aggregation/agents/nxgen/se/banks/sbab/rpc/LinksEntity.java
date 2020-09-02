package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LinksEntity {
    private String rel;
    private String href;
    private String type;
}
