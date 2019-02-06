package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserDataValue {
    private String lastlogonTimestamp;
    private String smid;
    private String muid;
    private String readonly;
    private String previousLogonTimestamp;
    private String daysPasswordStillValid;
    private String versionUse;
    private String muidCode;
    private int passwordChangeReason;
    private int tcFlag;

    public String getLastlogonTimestamp() {
        return lastlogonTimestamp;
    }

    public String getSmid() {
        return smid;
    }

    public String getMuid() {
        return muid;
    }

    public String getReadonly() {
        return readonly;
    }

    public String getPreviousLogonTimestamp() {
        return previousLogonTimestamp;
    }

    public String getDaysPasswordStillValid() {
        return daysPasswordStillValid;
    }

    public String getVersionUse() {
        return versionUse;
    }

    public String getMuidCode() {
        return muidCode;
    }

    public int getPasswordChangeReason() {
        return passwordChangeReason;
    }

    public int getTcFlag() {
        return tcFlag;
    }
}
