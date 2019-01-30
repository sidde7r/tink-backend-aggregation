package se.tink.backend.aggregation.agents.brokers.nordnet.model.Response;

import com.google.common.base.MoreObjects;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.ErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitBankIdResponse {
    private String orderRef;
    private String autoStartToken;
    private String collectUrl;
    private ErrorEntity error;

    public String getOrderRef() {
        return orderRef;
    }

    public String getAutoStartToken() {
        return autoStartToken;
    }

    public String getCollectUrl() {
        return collectUrl;
    }

    public ErrorEntity getError() {
        return error;
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
