package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LoginParameter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationEntity {
    private String userID;
    private List<AuthenticationDataEntity> authenticationData;
    private String consumerID;
    private String authenticationType;

    public AuthenticationEntity(String username, String password) {
        this.userID = LoginParameter.USER_VALUE_PREFIX + username;
        this.consumerID = LoginParameter.CONSUMER_ID;
        this.authenticationType = LoginParameter.AUTH_TYPE;
        this.authenticationData =
                Collections.singletonList(
                        new AuthenticationDataEntity(password, LoginParameter.AUTH_DATA_ID));
    }
}
