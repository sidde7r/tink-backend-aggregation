package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticateBankIdResponse {
    @JsonProperty("State")
    private Integer state;
    @JsonProperty("IsMobileDevice")
    private Boolean isMobileDevice;
    @JsonProperty("AutoStartUrl")
    private String autostartUrl;
    @JsonProperty("IsAjaxResponse")
    private boolean isAjaxResponse;
    @JsonProperty("Message")
    private FailMessage message;
    @JsonProperty("RedirectUrl")
    private String redirectUrl;
    @JsonProperty("UseBankidAppswitchUrl")
    private String useBankIdAppSwitchUrl;

    public String getAutostartUrl() {
        return autostartUrl;
    }

    public String getMessage() {
        return message != null ? message.getText() : "";
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getUseBankIdAppSwitchUrl() {
        return useBankIdAppSwitchUrl;
    }

    public boolean isAjaxResponse() {
        return isAjaxResponse;
    }

    public void setAjaxResponse(boolean isAjaxResponse) {
        this.isAjaxResponse = isAjaxResponse;
    }

    public void setAutostartUrl(String autostartUrl) {
        this.autostartUrl = autostartUrl;
    }

    public void setMessage(FailMessage message) {
        this.message = message;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public void setUseBankIdAppSwitchUrl(String useBankIdAppSwitchUrl) {
        this.useBankIdAppSwitchUrl = useBankIdAppSwitchUrl;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Boolean getIsMobileDevice() {
        return isMobileDevice;
    }

    public void setIsMobileDevice(Boolean isMobileDevice) {
        this.isMobileDevice = isMobileDevice;
    }
}
