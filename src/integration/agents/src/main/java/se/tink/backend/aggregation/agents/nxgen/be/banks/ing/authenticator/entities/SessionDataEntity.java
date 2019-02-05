package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionDataEntity {
    private String saltValidityPeriod;
    private String tokenLogon;
    private String migrationStopDate;
    private String oldEnrollStatus;
    private String openEnvironment;
    private String enrollStatus;
    private String facebookLink;
    private String oldTokenLogonStatus;
    private String logonStatus;
    private String oldLogonStatus;
    private String adobeTestAndTarget;
    private String twitterLink;
    private String pbMStatus;
    private String migrationStartDate;
    private String needsIngId;
    private String balanceBeforeLogon;
    private String salt;
    private String closedEnvironment;

    public String getSaltValidityPeriod() {
        return saltValidityPeriod;
    }

    public String getTokenLogon() {
        return tokenLogon;
    }

    public String getMigrationStopDate() {
        return migrationStopDate;
    }

    public String getOldEnrollStatus() {
        return oldEnrollStatus;
    }

    public String getOpenEnvironment() {
        return openEnvironment;
    }

    public String getEnrollStatus() {
        return enrollStatus;
    }

    public String getFacebookLink() {
        return facebookLink;
    }

    public String getOldTokenLogonStatus() {
        return oldTokenLogonStatus;
    }

    public String getLogonStatus() {
        return logonStatus;
    }

    public String getOldLogonStatus() {
        return oldLogonStatus;
    }

    public String getAdobeTestAndTarget() {
        return adobeTestAndTarget;
    }

    public String getTwitterLink() {
        return twitterLink;
    }

    public String getPbMStatus() {
        return pbMStatus;
    }

    public String getMigrationStartDate() {
        return migrationStartDate;
    }

    public String getNeedsIngId() {
        return needsIngId;
    }

    public String getBalanceBeforeLogon() {
        return balanceBeforeLogon;
    }

    public String getSalt() {
        return salt;
    }

    public String getClosedEnvironment() {
        return closedEnvironment;
    }
}
