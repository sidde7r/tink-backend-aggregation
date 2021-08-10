package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.dto.responses;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LinksEntity {
    private String rel;
    private String href;
    private String action;
}
