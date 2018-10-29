package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import com.fasterxml.jackson.annotation.JsonGetter;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class BaseResponse {

    List<String> error;
    List<String> errorFields;

    public List<String> getError() {
        return error;
    }

    @JsonGetter("error_fields")
    public List<String> getErrorFields() {
        return errorFields;
    }

    public boolean requestSuccessful() {
        return error == null;
    }

    public String getErrorMessage() {
        return StringUtils.join(error, ", ", ".");
    }
}
