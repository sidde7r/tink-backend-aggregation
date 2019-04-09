package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CollectMessage {
    @JsonProperty("Header")
    private String header;

    @JsonProperty("Text")
    private String text;

    @JsonProperty("ShowGetBankIdButton")
    private Boolean showGetBankIdButton;

    @JsonProperty("ShowRetryButton")
    private Boolean showRetryButton;

    @JsonProperty("ContinueCollect")
    private Boolean continueCollect;

    @JsonProperty("ShowManualStartButton")
    private Boolean showManualStartButton;

    @JsonProperty("ShowInstallBankIdButton")
    private Boolean showInstallBankIdButton;

    @JsonProperty("ShowContinueButton")
    private Boolean showContinueButton;

    @JsonProperty("ShowCancelButton")
    private Boolean showCancelButton;

    @JsonProperty("IsInformationMessage")
    private Boolean isInformationMessage;

    @JsonProperty("IsSuccessMessage")
    private Boolean isSuccessMessage;

    public String getHeader() {
        return header;
    }

    public String getText() {
        return text;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getGetShowGetBankIdButton() {
        return showGetBankIdButton;
    }

    public void setGetShowGetBankIdButton(Boolean getShowGetBankIdButton) {
        this.showGetBankIdButton = getShowGetBankIdButton;
    }

    public Boolean getShowRetryButton() {
        return showRetryButton;
    }

    public void setShowRetryButton(Boolean showRetryButton) {
        this.showRetryButton = showRetryButton;
    }

    public Boolean getContinueCollect() {
        return continueCollect;
    }

    public void setContinueCollect(Boolean continueCollect) {
        this.continueCollect = continueCollect;
    }

    public Boolean getShowManualStartButton() {
        return showManualStartButton;
    }

    public void setShowManualStartButton(Boolean showManualStartButton) {
        this.showManualStartButton = showManualStartButton;
    }

    public Boolean getShowInstallBankIdButton() {
        return showInstallBankIdButton;
    }

    public void setShowInstallBankIdButton(Boolean showInstallBankIdButton) {
        this.showInstallBankIdButton = showInstallBankIdButton;
    }

    public Boolean getShowContinueButton() {
        return showContinueButton;
    }

    public void setShowContinueButton(Boolean showContinueButton) {
        this.showContinueButton = showContinueButton;
    }

    public Boolean getShowCancelButton() {
        return showCancelButton;
    }

    public void setShowCancelButton(Boolean showCancelButton) {
        this.showCancelButton = showCancelButton;
    }

    public Boolean getIsInformationMessage() {
        return isInformationMessage;
    }

    public void setIsInformationMessage(Boolean isInformationMessage) {
        this.isInformationMessage = isInformationMessage;
    }

    public Boolean getIsSuccessMessage() {
        return isSuccessMessage;
    }

    public void setIsSuccessMessage(Boolean isSuccessMessage) {
        this.isSuccessMessage = isSuccessMessage;
    }
}
