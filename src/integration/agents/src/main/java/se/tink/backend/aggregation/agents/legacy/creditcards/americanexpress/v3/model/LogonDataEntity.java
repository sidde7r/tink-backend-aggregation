package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LogonDataEntity extends StatusEntity {
    private String gateKeeperCookie;
    private String cupcake;
    private String amexSession;
    private String publicGuid;
    private String statusCode;
    private ProfileDataEntity profileData;

    public String getGateKeeperCookie() {
        return gateKeeperCookie;
    }

    public void setGateKeeperCookie(String gateKeeperCookie) {
        this.gateKeeperCookie = gateKeeperCookie;
    }

    public String getCupcake() {
        return cupcake;
    }

    public void setCupcake(String cupcake) {
        this.cupcake = cupcake;
    }

    public String getAmexSession() {
        return amexSession;
    }

    public void setAmexSession(String amexSession) {
        this.amexSession = amexSession;
    }

    public String getPublicGuid() {
        return publicGuid;
    }

    public void setPublicGuid(String publicGuid) {
        this.publicGuid = publicGuid;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public ProfileDataEntity getProfileData() {
        return profileData;
    }

    public void setProfileData(ProfileDataEntity profileData) {
        this.profileData = profileData;
    }
}
