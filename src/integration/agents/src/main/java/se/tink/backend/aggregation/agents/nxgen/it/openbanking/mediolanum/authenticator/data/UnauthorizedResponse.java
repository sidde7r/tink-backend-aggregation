package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.authenticator.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
public class UnauthorizedResponse {

    private Result result;

    @JsonObject
    @Getter
    @NoArgsConstructor
    public static class Result {
        private String url;
    }
}
