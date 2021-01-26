package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc;

import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.AuthenticationStates;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LoginParameter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.UserCredentials;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities.AuthenticationDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities.AuthenticationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {
    private final AuthenticationEntity authentication;

    public LoginRequest(UserCredentials userCredentials, String otp, String multistepProcessId) {
        this.authentication =
                AuthenticationEntity.builder()
                        .withUserID(userCredentials.getUsername())
                        .withAuthenticationType(LoginParameter.AUTH_OTP_TYPE)
                        .withMultistepProcessId(multistepProcessId)
                        .withAuthenticationData(
                                Collections.singletonList(
                                        new AuthenticationDataEntity(
                                                otp, LoginParameter.OTP_DATA_ID)))
                        .build();
    }

    public LoginRequest(UserCredentials userCredentials, String multistepProcessId) {
        this.authentication =
                AuthenticationEntity.builder()
                        .withUserID(userCredentials.getUsername())
                        .withAuthenticationType(LoginParameter.AUTH_OTP_TYPE)
                        .withMultistepProcessId(multistepProcessId)
                        .withAuthenticationState(AuthenticationStates.GO_ON)
                        .build();
    }

    public LoginRequest(UserCredentials userCredentials) {
        this.authentication =
                AuthenticationEntity.builder()
                        .withUserID(userCredentials.getUsername())
                        .withAuthenticationType(LoginParameter.AUTH_TYPE)
                        .withAuthenticationData(
                                Collections.singletonList(
                                        new AuthenticationDataEntity(
                                                userCredentials.getPassword(),
                                                LoginParameter.AUTH_DATA_ID)))
                        .build();
    }
}
