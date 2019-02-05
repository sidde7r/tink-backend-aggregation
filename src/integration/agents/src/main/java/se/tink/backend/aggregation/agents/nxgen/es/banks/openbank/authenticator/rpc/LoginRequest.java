package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.entities.MobileDeviceInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {
    @JsonProperty("document")
    private String username;

    @JsonProperty("documentType")
    private String usernameType;

    private int force;
    private MobileDeviceInfoEntity mobileDeviceInfo;
    private String osVersion;
    private String password;
    private String uuid;

    private LoginRequest(Builder builder) {
        username = builder.username;
        usernameType = builder.usernameType;
        force = builder.force;
        mobileDeviceInfo = builder.mobileDeviceInfo;
        osVersion = builder.osVersion;
        password = builder.password;
        uuid = builder.uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getUsernameType() {
        return usernameType;
    }

    public int getForce() {
        return force;
    }

    public MobileDeviceInfoEntity getMobileDeviceInfo() {
        return mobileDeviceInfo;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getPassword() {
        return password;
    }

    public String getUuid() {
        return uuid;
    }

    public static class Builder {
        private String username;
        private String usernameType;
        private int force;
        private MobileDeviceInfoEntity mobileDeviceInfo;
        private String osVersion;
        private String password;
        private String uuid;

        public LoginRequest.Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public LoginRequest.Builder withUsernameType(String usernameType) {
            this.usernameType = usernameType;
            return this;
        }

        public LoginRequest.Builder withForce(int force) {
            this.force = force;
            return this;
        }

        public LoginRequest.Builder withMobileDeviceInfo(MobileDeviceInfoEntity mobileDeviceInfo) {
            this.mobileDeviceInfo = mobileDeviceInfo;
            return this;
        }

        public LoginRequest.Builder withOsVersion(String osVersion) {
            this.osVersion = osVersion;
            return this;
        }

        public LoginRequest.Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public LoginRequest.Builder withUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public LoginRequest build() {
            return new LoginRequest(this);
        }
    }
}
