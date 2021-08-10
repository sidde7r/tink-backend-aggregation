package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.dto.responses;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PageInfoEntity {
    private String previousPage;
    private String nextPage;
}
