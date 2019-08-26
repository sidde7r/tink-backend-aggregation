package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class GroupHeader {

    @JsonProperty("http_code")
    private int httpCode;

    @JsonProperty("message_identification")
    private String messageIdentification;

    @JsonProperty("creation_date_time")
    private String creationDateTime;

    public int getHttpCode() {
        return httpCode;
    }

    public String getMessageIdentification() {
        return messageIdentification;
    }

    public String getCreationDateTime() {
        return creationDateTime;
    }
}
