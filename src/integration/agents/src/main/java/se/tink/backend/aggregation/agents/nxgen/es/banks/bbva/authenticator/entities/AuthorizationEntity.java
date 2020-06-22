package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LoginParameter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizationEntity {
    private String userId;
    private List<AuthenticationDataEntity> authenticationData;
    private String multistepProcessId;
    private String consumerID;
    private String authenticationType;

    public AuthorizationEntity(String username, String otpCode, String multistepProcessId) {
        this.userId = LoginParameter.USER_VALUE_PREFIX + username;
        this.authenticationData =
                Collections.singletonList(
                        new AuthenticationDataEntity(otpCode, LoginParameter.OTP_DATA_ID));
        this.multistepProcessId = multistepProcessId;
        this.consumerID = LoginParameter.CONSUMER_ID;
        this.authenticationType = LoginParameter.AUTHORIZE_TYPE;
    }
}
