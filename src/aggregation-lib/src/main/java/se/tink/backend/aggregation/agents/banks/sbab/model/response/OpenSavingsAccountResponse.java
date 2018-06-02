package se.tink.backend.aggregation.agents.banks.sbab.model.response;

public class OpenSavingsAccountResponse {

    private String token;
    private String strutsTokenName;
    private String confirmUrl;
    private String referer;

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

    public String getConfirmUrl() {
        return confirmUrl;
    }

    public void setConfirmUrl(String confirmUrl) {
        this.confirmUrl = confirmUrl;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }
}
