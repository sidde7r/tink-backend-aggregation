package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DeviceDetailsEntity {

    private String applicationState = "active";
    private String biometricDbHash = StringUtils.EMPTY;
    private String connection = "wifi: 192.168.99.99";
    private String deviceId;
    private String deviceModel = "iPhone9,3";
    private String deviceName;
    private List<String> filesystemKclog = Collections.singletonList("");
    private boolean hasHwSecurity = true;
    private String hwType = "iPhone";
    private boolean jailbroken = false;
    private List keychainErrLogs = Collections.EMPTY_LIST;
    private int loggedUsers = 0;
    private String osType = "iPhone";
    private String osVersion = "13.3.1";
    private boolean screenLock = true;
    private int sflags = -9;
    private String simOperator = "(null)(null)";
    private boolean tampered = false;
    private String tz = "Europe/Stockholm";

    public DeviceDetailsEntity(String deviceId, String deviceName) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
    }
}
