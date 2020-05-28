package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String firstAuth;

    private String login;
    private String matrixRandomChallenge;
    private String password;
    private String udid;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String deviceEnrolmentTokenValue;

    private LoginRequest(
            String firstAuth,
            String login,
            String matrixRandomChallenge,
            String password,
            String udid,
            String deviceEnrolmentTokenValue) {
        this.firstAuth = firstAuth;
        this.login = login;
        this.matrixRandomChallenge = matrixRandomChallenge;
        this.password = password;
        this.udid = udid;
        this.deviceEnrolmentTokenValue = deviceEnrolmentTokenValue;
    }

    public static LoginRequest createFirstLogin(
            String firstAuth,
            String login,
            String matrixRandomChallenge,
            String password,
            String udid) {
        return new LoginRequest(firstAuth, login, matrixRandomChallenge, password, udid, null);
    }

    public static LoginRequest createRepeatedDeviceLogin(
            String login,
            String matrixRandomChallenge,
            String password,
            String udid,
            String deviceEnrolmentTokenValue) {
        return new LoginRequest(
                null, login, matrixRandomChallenge, password, udid, deviceEnrolmentTokenValue);
    }
}
