package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationEntity {
    private List<AuthenticationDataItemEntity> authenticationData = new ArrayList<>();
    private String consumerID;
    private String authenticationType;
    private String userID;

    public AuthenticationEntity(String password, String phonenumber) {
        this.authenticationData.add(new AuthenticationDataItemEntity(password));
        this.consumerID = BBVAConstants.VALUES.CONSUMER_ID;
        this.authenticationType = BBVAConstants.VALUES.AUTHENTICATION_PASSWORD;
        this.userID = phonenumber;
    }
}
