package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;

public class LoginSotpRequest {
    private final TypeValuePair fingerprint;
    private final TypeValuePair osVersion;
    private final TypeValuePair deviceId;
    private final TypeValuePair company;
    private final TypeValuePair os;
    private final TypeValuePair appType;
    private final TypeValuePair deviceType;
    private final TypeValuePair otp;
    private final TypeValuePair language;
    private final TypeValuePair username;
    private final TypeValuePair appVersion; // "18.2.0"
    private final TypeValuePair applicationId; // "A031"
    private final TypeValuePair withTouchId;

    public LoginSotpRequest(
            TypeValuePair fingerprint,
            TypeValuePair osVersion,
            TypeValuePair deviceId,
            TypeValuePair company,
            TypeValuePair os,
            TypeValuePair appType,
            TypeValuePair deviceType,
            TypeValuePair otp,
            TypeValuePair language,
            TypeValuePair username,
            TypeValuePair appVersion,
            TypeValuePair applicationId,
            TypeValuePair withTouchId) {
        this.fingerprint = fingerprint;
        this.osVersion = osVersion;
        this.deviceId = deviceId;
        this.company = company;
        this.os = os;
        this.appType = appType;
        this.deviceType = deviceType;
        this.otp = otp;
        this.language = language;
        this.username = username;
        this.appVersion = appVersion;
        this.applicationId = applicationId;
        this.withTouchId = withTouchId;
    }

    public TypeValuePair getFingerprint() {
        return fingerprint;
    }

    public TypeValuePair getOsVersion() {
        return osVersion;
    }

    public TypeValuePair getDeviceId() {
        return deviceId;
    }

    public TypeValuePair getCompany() {
        return company;
    }

    public TypeValuePair getOs() {
        return os;
    }

    public TypeValuePair getAppType() {
        return appType;
    }

    public TypeValuePair getDeviceType() {
        return deviceType;
    }

    public TypeValuePair getOtp() {
        return otp;
    }

    public TypeValuePair getLanguage() {
        return language;
    }

    public TypeValuePair getUsername() {
        return username;
    }

    public TypeValuePair getAppVersion() {
        return appVersion;
    }

    public TypeValuePair getApplicationId() {
        return applicationId;
    }

    public TypeValuePair getWithTouchId() {
        return withTouchId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TypeValuePair fingerprint;
        private TypeValuePair osVersion;
        private TypeValuePair deviceId;
        private TypeValuePair company;
        private TypeValuePair os;
        private TypeValuePair appType;
        private TypeValuePair deviceType;
        private TypeValuePair otp;
        private TypeValuePair language;
        private TypeValuePair username;
        private TypeValuePair appVersion;
        private TypeValuePair applicationId;
        private TypeValuePair withTouchId;

        public Builder setFingerprint(String fingerprint) {
            this.fingerprint = TypeValuePair.createText(fingerprint);
            return this;
        }

        public Builder setOsVersion(String osVersion) {
            this.osVersion = TypeValuePair.createText(osVersion);
            return this;
        }

        public Builder setDeviceId(String deviceId) {
            this.deviceId = TypeValuePair.createText(deviceId);
            return this;
        }

        public Builder setCompany(String company) {
            this.company = TypeValuePair.createText(company);
            return this;
        }

        public Builder setOs(String os) {
            this.os = TypeValuePair.createText(os);
            return this;
        }

        public Builder setAppType(String appType) {
            this.appType = TypeValuePair.createText(appType);
            return this;
        }

        public Builder setDeviceType(String deviceType) {
            this.deviceType = TypeValuePair.createText(deviceType);
            return this;
        }

        public Builder setOtp(String otp) {
            this.otp = TypeValuePair.createText(otp);
            return this;
        }

        public Builder setUsername(String username) {
            this.username = TypeValuePair.createText(username);
            return this;
        }

        public Builder setAppVersion(String appVersion) {
            this.appVersion = TypeValuePair.createText(appVersion);
            return this;
        }

        public Builder setApplicationId(String applicationId) {
            this.applicationId = TypeValuePair.createText(applicationId);
            return this;
        }

        public Builder setWithTouchId(String withTouchId) {
            this.withTouchId = TypeValuePair.createText(withTouchId);
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = TypeValuePair.createText(language);
            return this;
        }

        public LoginSotpRequest build() {
            return new LoginSotpRequest(
                    fingerprint,
                    osVersion,
                    deviceId,
                    company,
                    os,
                    appType,
                    deviceType,
                    otp,
                    language,
                    username,
                    appVersion,
                    applicationId,
                    withTouchId);
        }
    }
}
