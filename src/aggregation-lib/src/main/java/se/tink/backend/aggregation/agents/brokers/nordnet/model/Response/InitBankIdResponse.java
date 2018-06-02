package se.tink.backend.aggregation.agents.brokers.nordnet.model.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitBankIdResponse {
    private String orderRef;
    private String autoStartToken;
    private String collectUrl;

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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("orderRef", orderRef)
                .add("autoStartToken", autoStartToken)
                .add("collectUrl", collectUrl)
                .toString();
    }
}
