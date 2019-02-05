package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MobileDeviceInfoEntity {
    private String device;
    private String platform;
    private Long pushEnabled;
    private Boolean rooted;
    private String version;

    private MobileDeviceInfoEntity(Builder builder) {
        device = builder.device;
        platform = builder.platform;
        pushEnabled = builder.pushEnabled;
        rooted = builder.rooted;
        version = builder.version;
    }

    public String getDevice() {
        return device;
    }

    public String getPlatform() {
        return platform;
    }

    public Long getPushEnabled() {
        return pushEnabled;
    }

    public Boolean getRooted() {
        return rooted;
    }

    public String getVersion() {
        return version;
    }

    public static class Builder {
        private String device;
        private String platform;
        private Long pushEnabled;
        private Boolean rooted;
        private String version;

        public MobileDeviceInfoEntity.Builder withDevice(String device) {
            this.device = device;
            return this;
        }

        public MobileDeviceInfoEntity.Builder withPlatform(String platform) {
            this.platform = platform;
            return this;
        }

        public MobileDeviceInfoEntity.Builder withPushEnabled(Long pushEnabled) {
            this.pushEnabled = pushEnabled;
            return this;
        }

        public MobileDeviceInfoEntity.Builder withRooted(Boolean rooted) {
            this.rooted = rooted;
            return this;
        }

        public MobileDeviceInfoEntity.Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public MobileDeviceInfoEntity build() {
            return new MobileDeviceInfoEntity(this);
        }
    }
}
