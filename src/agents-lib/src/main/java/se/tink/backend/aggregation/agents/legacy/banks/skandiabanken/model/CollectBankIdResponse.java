package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CollectBankIdResponse {
    @JsonProperty("IsAjaxResponse")
    private boolean isAjaxResponse;
    @JsonProperty("Message")
    private CollectMessage message;
    @JsonProperty("State")
    private int state;
    @JsonProperty("ProgressbarMessage")
    private String progressbarMessage;
    @JsonProperty("RedirectUrl")
    private String redirectUrl;

    public CollectMessage getMessage() {
        return message;
    }

    public int getState() {
        return state;
    }

    public boolean isAjaxResponse() {
        return isAjaxResponse;
    }

    public void setAjaxResponse(boolean isAjaxResponse) {
        this.isAjaxResponse = isAjaxResponse;
    }

    public void setMessage(CollectMessage message) {
        this.message = message;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getProgressbarMessage() {
        return progressbarMessage;
    }

    public void setProgressbarMessage(String progressbarMessage) {
        this.progressbarMessage = progressbarMessage;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

}
