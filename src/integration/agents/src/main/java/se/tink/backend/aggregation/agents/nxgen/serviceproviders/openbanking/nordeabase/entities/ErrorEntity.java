package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.enums.NordeaFailures;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    @JsonProperty("_type")
    private String type;

    @JsonProperty("http_code")
    private int httpCode;

    private RequestEntity request;
    private List<FailuresEntity> failures;

    public String getType() {
        return type;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void parseAndThrow() throws Exception {
        if ("ExternalError".equals(type)) {
            FailuresEntity failure =
                    failures.stream()
                            .findAny()
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Got an error without failures from Nordea."));

            NordeaFailures.mapNordeaFailureToException(failure.getCode(), failure.getDescription());
        }
    }
}
