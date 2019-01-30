package se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.model.ErrorEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderBankIdResponse {
    private String orderRef;
    private String autoStartToken;
    private String collectUrl;
    private ErrorEntity error;

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

    public ErrorEntity getError() {
        return error;
    }

    public void setError(ErrorEntity error) {
        this.error = error;
    }
}
