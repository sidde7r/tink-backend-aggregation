package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import java.util.List;

public class InitialTransferResponse {

    private String token;
    private String strutsTokenName;
    private String postUrl;
    private List<SavedRecipientEntity> validRecipients;
    private List<String> validSourceAccountNumbers;
    private String saveRecipientStrutsTokenName;
    private String saveRecipientToken;
    private String saveRecipientPostUrl;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStrutsTokenName() {
        return strutsTokenName;
    }

    public void setStrutsTokenName(String strutsTokenName) {
        this.strutsTokenName = strutsTokenName;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public void setValidRecipients(List<SavedRecipientEntity> validRecipients) {
        this.validRecipients = validRecipients;
    }

    public List<SavedRecipientEntity> getValidRecipients() {
        return validRecipients;
    }

    public void setValidSourceAccountNumbers(List<String> validSourceAccountNumbers) {
        this.validSourceAccountNumbers = validSourceAccountNumbers;
    }

    public List<String> getValidSourceAccountNumbers() {
        return validSourceAccountNumbers;
    }

    public void setSaveRecipientStrutsTokenName(String saveRecipientStrutsTokenName) {
        this.saveRecipientStrutsTokenName = saveRecipientStrutsTokenName;
    }

    public void setSaveRecipientToken(String saveRecipientToken) {
        this.saveRecipientToken = saveRecipientToken;
    }

    public String getSaveRecipientStrutsTokenName() {
        return saveRecipientStrutsTokenName;
    }

    public String getSaveRecipientToken() {
        return saveRecipientToken;
    }

    public void setSaveRecipientPostUrl(String saveRecipientPostUrl) {
        this.saveRecipientPostUrl = saveRecipientPostUrl;
    }

    public String getSaveRecipientPostUrl() {
        return saveRecipientPostUrl;
    }
}
