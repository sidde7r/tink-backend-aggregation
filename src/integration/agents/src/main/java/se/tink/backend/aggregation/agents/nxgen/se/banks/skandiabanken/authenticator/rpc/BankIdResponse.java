package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.entities.MessageEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdResponse {
    @JsonProperty("ProgressbarMessage")
    private String progressbarMessage = "";

    @JsonProperty("State")
    private int state;

    @JsonProperty("Message")
    private MessageEntity message;

    @JsonProperty("RedirectUrl")
    private String redirectUrl = "";

    @JsonProperty("IsAjaxResponse")
    private boolean isAjaxResponse;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setMessage(MessageEntity message) {
        this.message = message;
    }

    @JsonIgnore
    public MessageEntity getMessage() {
        return message;
    }

    @JsonIgnore
    public String getRedirectUrl() {
        return redirectUrl;
    }
}
