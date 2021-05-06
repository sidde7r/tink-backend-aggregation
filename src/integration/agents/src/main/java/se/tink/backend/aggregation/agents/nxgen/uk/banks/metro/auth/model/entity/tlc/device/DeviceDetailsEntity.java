package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonNaming(value = SnakeCaseStrategy.class)
@JsonObject
@Builder
public class DeviceDetailsEntity {
    private final int loggedUsers;
    private final String biometricDbHash;
    private final String keychainErrLogs;
    private final String connection;
    private final boolean tampered;
    private final boolean jailbroken;
    private final boolean screenLock;
    private final String tz;
    private final String applicationState;
    private final boolean hasHwSecurity;
    private final int sflags;
    private final String hwType;
    private final String deviceName;
    private final String simOperatorName;
    private final WifiNetworkEntity wifiNetwork;
    private final String deviceId;
    private final String osVersion;
    private final String simOperator;
    private final String osType;
    private final String deviceModel;

    @Builder
    @AllArgsConstructor
    @JsonObject
    static class WifiNetworkEntity {
        private final String bssid;
        private final String ssid;
    }

    public static DeviceDetailsEntity getDefault() {
        return DeviceDetailsEntity.builder()
                .loggedUsers(0)
                .biometricDbHash("")
                .keychainErrLogs("")
                .connection("wifi: 10.28.144.135")
                .tampered(false)
                .jailbroken(false)
                .screenLock(true)
                .tz("Europe/Stockholm")
                .applicationState("active")
                .hasHwSecurity(true)
                .sflags(-9)
                .hwType("iPhone")
                .deviceName("93d89712e723aca52f920e872310d06c")
                .deviceId("46DCC2CD-A375-40E6-BD57-32F76D784435")
                .osVersion("12.4.5")
                .simOperator("(null)(null)")
                .simOperatorName("Telia N")
                .wifiNetwork(
                        new WifiNetworkEntity(
                                "c7aa7e33a5b16b3c7316f6346d3cf0bb",
                                "38561bf2d959bcbde7817ab7b7b47c2c"))
                .deviceModel("iPhone7,2")
                .osType("iPhone")
                .build();
    }
}
