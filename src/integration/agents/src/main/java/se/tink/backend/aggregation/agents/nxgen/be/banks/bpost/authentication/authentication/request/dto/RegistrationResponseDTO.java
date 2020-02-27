package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegistrationResponseDTO implements AuthenticationResponseState {

    private static final String ERROR_CODE_INCORRECT_RESPONSE = "XXXX1214";
    private static final String ERROR_CODE_ACCOUNT_BLOCKED = "XXXX1673";

    @JsonProperty("Order")
    private OrderDTO order;

    private String localizedMessage;

    private String errorCode;

    public String getChallengeCode() {
        return order.credentials.challenge;
    }

    public String getOrderReference() {
        return order.orderReference;
    }

    public boolean isError() {
        return order != null && order.isError;
    }

    public boolean isErrorChallengeResponseIncorrect() {
        return ERROR_CODE_INCORRECT_RESPONSE.equals(errorCode);
    }

    public boolean isErrorAccountBlocked() {
        return ERROR_CODE_ACCOUNT_BLOCKED.equals(errorCode);
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
