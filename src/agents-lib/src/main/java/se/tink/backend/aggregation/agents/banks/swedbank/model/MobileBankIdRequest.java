package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileBankIdRequest {
    private String easyLoginId;
    private boolean generateEasyLoginId;
    private boolean useEasyLogin;
    private String userId;

    public String getEasyLoginId() {
        return easyLoginId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isGenerateEasyLoginId() {
        return generateEasyLoginId;
    }

    public boolean isUseEasyLogin() {
        return useEasyLogin;
    }

    public void setEasyLoginId(String easyLoginId) {
        this.easyLoginId = easyLoginId;
    }

    public void setGenerateEasyLoginId(boolean generateEasyLoginId) {
        this.generateEasyLoginId = generateEasyLoginId;
    }

    public void setUseEasyLogin(boolean useEasyLogin) {
        this.useEasyLogin = useEasyLogin;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
