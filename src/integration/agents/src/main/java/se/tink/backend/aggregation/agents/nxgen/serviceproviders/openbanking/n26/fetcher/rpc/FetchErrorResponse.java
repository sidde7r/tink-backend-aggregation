package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.rpc;

import lombok.Getter;
import lombok.ToString;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@ToString
public class FetchErrorResponse {
    private String title;
    private String code;
    private String detail;
}
