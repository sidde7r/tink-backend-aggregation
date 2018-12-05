package se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateBankIdResponse {
    private String orderRef;
    private String autoStartToken;
    private String collectUrl;
    private ErrorEntity error;

    public ErrorEntity getError() {
        return error;
    }

    public void setError(ErrorEntity error) {
        this.error = error;
    }

    public String getOrderRef() {
        return orderRef;
    }

    public void setOrderRef(String orderRef) {
        this.orderRef = orderRef;
    }

    public String getAutoStartToken() {
        return autoStartToken;
    }

    public void setAutoStartToken(String autoStartToken) {
        this.autoStartToken = autoStartToken;
    }

    public String getCollectUrl() {
        return collectUrl;
    }

    public void setCollectUrl(String collectUrl) {
        this.collectUrl = collectUrl;
    }
}
