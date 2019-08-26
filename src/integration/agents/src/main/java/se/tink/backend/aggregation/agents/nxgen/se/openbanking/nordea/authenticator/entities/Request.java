package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Request {

    @JsonProperty("message_identifier")
    private String messageIdentifier;

    @JsonProperty("_type")
    private String type;

    @JsonProperty("url")
    private String url;

    public String getMessageIdentifier() {
        return messageIdentifier;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }
}
