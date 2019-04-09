package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConstants.StatusCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.entities.ProfileDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LogonDataEntity {
    private int status;
    private ProfileDataEntity profileData;
    private String gateKeeperCookie;
    private String cupcake;
    private String amexSession;
    private String publicGuid;
    private String statusCode;
    private String message;

    public int getStatus() {
        return status;
    }

    public ProfileDataEntity getProfileData() {
        return profileData;
    }

    public String getGateKeeperCookie() {
        return gateKeeperCookie;
    }

    public String getCupcake() {
        return cupcake;
    }

    public String getAmexSession() {
        return amexSession;
    }

    public String getPublicGuid() {
        return publicGuid;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public boolean loginSucceed() {
        return this.getStatus() == 0;
    }

    public boolean loginFailed() {
        return this.getStatus() == -1;
    }

    public boolean isRevoked() {
        return StatusCode.REVOKED.equalsIgnoreCase(statusCode);
    }

    public boolean isIncorrect() {
        return StatusCode.INCORRECT.equalsIgnoreCase(statusCode)
                || StatusCode.SECOND_ATTEMPT.equalsIgnoreCase(statusCode);
    }
}
