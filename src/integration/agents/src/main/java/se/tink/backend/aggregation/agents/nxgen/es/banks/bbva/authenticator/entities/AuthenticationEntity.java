package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.AuthenticationStates;
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
    private String authenticationState;

    public AuthenticationEntity(String username, String password, String multistepProcessId) {
        this.userID = LoginParameter.USER_VALUE_PREFIX + username;
        this.consumerID = LoginParameter.CONSUMER_ID;
        this.authenticationType = LoginParameter.AUTH_TYPE;
        this.authenticationData =
                Collections.singletonList(
                        new AuthenticationDataEntity(password, getDataId(multistepProcessId)));
        this.multistepProcessId = multistepProcessId;
    }

    public AuthenticationEntity(String username, String multistepProcessId) {
        this.userID = LoginParameter.USER_VALUE_PREFIX + username;
        this.consumerID = LoginParameter.CONSUMER_ID;
        this.authenticationType = LoginParameter.AUTH_TYPE;
        this.multistepProcessId = multistepProcessId;
        this.authenticationState = AuthenticationStates.GO_ON;
        this.authenticationData = Collections.emptyList();
    }

    private String getDataId(String multistepProcessId) {
        return multistepProcessId == null
                ? LoginParameter.AUTH_DATA_ID
                : LoginParameter.OTP_DATA_ID;
    }
}
