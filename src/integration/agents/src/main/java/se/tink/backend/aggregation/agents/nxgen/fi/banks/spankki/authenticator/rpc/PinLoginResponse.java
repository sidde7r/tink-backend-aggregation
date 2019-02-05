package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.entities.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PinLoginResponse extends SpankkiResponse {
    private CustomerEntity customer;
    private Boolean fundsFeatureDisabled;
    private List<String> features;
    private String sessionKey;

    public CustomerEntity getCustomer() {
        return customer;
    }

    public Boolean getFundsFeatureDisabled() {
        return fundsFeatureDisabled;
    }

    public List<String> getFeatures() {
        return features;
    }

    public String getSessionKey() {
        return sessionKey;
    }
}
