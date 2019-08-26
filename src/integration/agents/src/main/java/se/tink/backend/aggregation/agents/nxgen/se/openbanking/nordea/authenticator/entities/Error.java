package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Error {

    @JsonProperty("request")
    private Request request;

    @JsonProperty("failures")
    private List<FailuresItem> failures;

    @JsonProperty("http_code")
    private int httpCode;

    @JsonProperty("_type")
    private String type;

    public Request getRequest() {
        return request;
    }

    public List<FailuresItem> getFailures() {
        return failures;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getType() {
        return type;
    }
}
