package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class BaseResponse {

    @JsonProperty("error")
    List<String> error;

    @JsonProperty("error_fields")
    List<String> errorFields;

    @JsonIgnore
    public Optional<List<String>> getError() {
        return Optional.ofNullable(error);
    }

    @JsonIgnore
    public Optional<List<String>> getErrorFields() {
        return Optional.ofNullable(errorFields);
    }

    @JsonIgnore
    public boolean requestSuccessful() {
        return error == null;
    }

    @JsonIgnore
    public boolean requestFailed() {
        return error != null;
    }

    @JsonIgnore
    public String getErrorMessage() {
        if (requestSuccessful()) {
            return "";
        }

        return StringUtils.join(error, ", ", ".");
    }
}
