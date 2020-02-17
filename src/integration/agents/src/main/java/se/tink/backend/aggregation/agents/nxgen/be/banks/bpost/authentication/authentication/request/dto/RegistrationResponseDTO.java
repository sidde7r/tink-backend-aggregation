package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegistrationResponseDTO implements AuthenticationResponseState {

    @JsonProperty("Order")
    private OrderDTO order;

    public String getChallengeCode() {
        return order.credentials.challenge;
    }

    public String getOrderReference() {
        return order.orderReference;
    }

    public boolean isError() {
        return order.isError;
    }

    @Override
    public String getState() {
        return order.state;
    }

    public String getSessionToken() {
        return order.sessionToken;
    }

    public String getDeviceInstallationID() {
        return order.goal.deviceInstallationID;
    }
}
