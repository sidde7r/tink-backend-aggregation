package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.entities.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenLoginResponse extends SpankkiResponse {
    private CustomerEntity customer;
    private boolean isPAD;
    private boolean fundsFeatureDisabled;
    private List<String> features;
    private List<String> serviceCodes;
    private String newToken;
    private String passwordStatus;
    private String passwordStatusMessage;
    private String sessionKey;

    @JsonIgnore
    public boolean isMustChangePassword() {
        return SpankkiConstants.Authentication.PASSWORD_STATUS_CHANGE.equalsIgnoreCase(passwordStatus);
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public boolean isPAD() {
        return isPAD;
    }

    public boolean isFundsFeatureDisabled() {
        return fundsFeatureDisabled;
    }

    public List<String> getFeatures() {
        return features;
    }

    public List<String> getServiceCodes() {
        return serviceCodes;
    }

    public String getNewToken() {
        return newToken;
    }

    public String getPasswordStatus() {
        return passwordStatus;
    }

    public String getPasswordStatusMessage() {
        return passwordStatusMessage;
    }

    public String getSessionKey() {
        return sessionKey;
    }
}
