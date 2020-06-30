package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LoginParameter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AuthenticationEntity {
    private String userID;
    private List<AuthenticationDataEntity> authenticationData;
    private String consumerID;
    private String authenticationType;
    private String multistepProcessId;

    public AuthenticationEntity(String username, String password, String multistepProcessId) {
        this.userID = LoginParameter.USER_VALUE_PREFIX + username;
        this.consumerID = LoginParameter.CONSUMER_ID;
        this.authenticationType = LoginParameter.AUTH_TYPE;
        this.authenticationData =
                Collections.singletonList(
                        new AuthenticationDataEntity(password, getDataId(multistepProcessId)));
        this.multistepProcessId = multistepProcessId;
    }

    private String getDataId(String multistepProcessId) {
        return multistepProcessId == null
                ? LoginParameter.AUTH_DATA_ID
                : LoginParameter.OTP_DATA_ID;
    }

    public AuthenticationEntity(String username, String otpCode, String multistepProcessId) {
        this.userID = LoginParameter.USER_VALUE_PREFIX + username;
        this.authenticationData =
                Collections.singletonList(
                        new AuthenticationDataEntity(otpCode, LoginParameter.OTP_DATA_ID));
        this.multistepProcessId = multistepProcessId;
        this.consumerID = LoginParameter.CONSUMER_ID;
        this.authenticationType = LoginParameter.AUTH_OTP_TYPE;
    }
}
