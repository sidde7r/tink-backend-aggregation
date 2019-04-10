package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.control.Option;
import se.tink.backend.aggregation.annotations.JsonObject;
import static io.vavr.Predicates.not;

@JsonObject
public class ErrorResponse {
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    public boolean hasError() {
        return getError().filter(not(String::isEmpty)).isDefined();
    }

    public boolean hasErrorCode(String errorCode) {
        return getError().filter(e -> e.equalsIgnoreCase(errorCode)).isDefined();
    }

    public boolean hasErrorDescription() {
        return getErrorDescription().filter(not(String::isEmpty)).isDefined();
    }

    public Option<String> getError() {
        return Option.of(error);
    }

    public Option<String> getErrorDescription() {
        return Option.of(errorDescription);
    }
}
