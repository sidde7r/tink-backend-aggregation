package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserDataEntity {
    private String readonly;
    private String lastlogonTimestamp;
    private String previousLogonTimestamp;
    private String smid;
    private int passwordChangeReason;
    private int tcFlag;
    private String versionUse;
    private String muid;
    private String muidCode;
    private String daysPasswordStillValid;
}
