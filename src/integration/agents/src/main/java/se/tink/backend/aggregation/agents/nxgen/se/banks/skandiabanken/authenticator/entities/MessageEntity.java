package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MessageEntity {
    @JsonProperty("ShowGetBankIdButton")
    private boolean showGetBankIdButton;

    @JsonProperty("ShowRetryButton")
    private boolean showRetryButton;

    @JsonProperty("ContinueCollect")
    private boolean continueCollect;

    @JsonProperty("ShowManualStartButton")
    private boolean showManualStartButton;

    @JsonProperty("ShowInstallBankIdButton")
    private boolean showInstallBankIdButton;

    @JsonProperty("QrCodeVisible")
    private boolean qrCodeVisible;

    @JsonProperty("QrCodeImage")
    private String qrCodeImage = "";

    @JsonProperty("Header")
    private String header = "";

    @JsonProperty("Text")
    private String text = "";

    @JsonProperty("ShowContinueButton")
    private boolean showContinueButton;

    @JsonProperty("ShowCancelButton")
    private boolean showCancelButton;

    @JsonProperty("IsInformationMessage")
    private boolean isInformationMessage;

    @JsonProperty("IsSuccessMessage")
    private boolean isSuccessMessage;

    @JsonIgnore
    public boolean isContinueCollect() {
        return continueCollect;
    }

    @JsonIgnore
    public boolean isShowInstallBankIdButton() {
        return showInstallBankIdButton;
    }

    @JsonIgnore
    public boolean isShowRetryButton() {
        return showRetryButton;
    }
}
