package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import com.fasterxml.jackson.annotation.JsonGetter;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

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
        String result = new String();
        if (error != null) {
            for (int i = 0; i < error.size(); ++i) {
                result += error.get(i);
                if (i != error.size() - 1)
                {
                    result += ", ";
                } else {
                    result += ".";
                }
            }
        }
        return result;
    }
}
