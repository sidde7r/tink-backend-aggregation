package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;

public class ChallengeSotpRequest {
    private final TypeValuePair deviceName;
    private final TypeValuePair applicationTypeCode;
    private final TypeValuePair logonId;
    private final TypeValuePair language;
    private final TypeValuePair osVersionNo;
    private final TypeValuePair deviceId;
    private final TypeValuePair fingerprint;
    private final TypeValuePair osType;
    private final TypeValuePair applicationId;
    private final TypeValuePair companyNo;
    private final TypeValuePair applicationVersionNo;

    public ChallengeSotpRequest(
            TypeValuePair deviceName,
            TypeValuePair applicationTypeCode,
            TypeValuePair logonId,
            TypeValuePair language,
            TypeValuePair osVersionNo,
            TypeValuePair deviceId,
            TypeValuePair fingerprint,
            TypeValuePair osType,
            TypeValuePair applicationId,
            TypeValuePair companyNo,
            TypeValuePair applicationVersionNo) {
        this.deviceName = deviceName;
        this.applicationTypeCode = applicationTypeCode;
        this.logonId = logonId;
        this.language = language;
        this.osVersionNo = osVersionNo;
        this.deviceId = deviceId;
        this.fingerprint = fingerprint;
        this.osType = osType;
        this.applicationId = applicationId;
        this.companyNo = companyNo;
        this.applicationVersionNo = applicationVersionNo;
    }

    public TypeValuePair getDeviceName() {
        return deviceName;
    }

    public TypeValuePair getApplicationTypeCode() {
        return applicationTypeCode;
    }

    public TypeValuePair getLogonId() {
        return logonId;
    }

    public TypeValuePair getLanguage() {
        return language;
    }

    public TypeValuePair getOsVersionNo() {
        return osVersionNo;
    }

    public TypeValuePair getDeviceId() {
        return deviceId;
    }

    public TypeValuePair getFingerprint() {
        return fingerprint;
    }

    public TypeValuePair getOsType() {
        return osType;
    }

    public TypeValuePair getApplicationId() {
        return applicationId;
    }

    public TypeValuePair getCompanyNo() {
        return companyNo;
    }

    public TypeValuePair getApplicationVersionNo() {
        return applicationVersionNo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TypeValuePair deviceName;
        private TypeValuePair applicationTypeCode;
        private TypeValuePair logonId;
        private TypeValuePair language;
        private TypeValuePair osVersionNo;
        private TypeValuePair deviceId;
        private TypeValuePair fingerprint;
        private TypeValuePair osType;
        private TypeValuePair applicationId;
        private TypeValuePair companyNo;
        private TypeValuePair applicationVersionNo;

        public Builder setDeviceName(String deviceName) {
            this.deviceName = TypeValuePair.createText(deviceName);
            return this;
        }

        public Builder setApplicationTypeCode(String applicationTypeCode) {
            this.applicationTypeCode = TypeValuePair.createText(applicationTypeCode);
            return this;
        }

        public Builder setLogonId(String logonId) {
            this.logonId = TypeValuePair.createText(logonId);
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = TypeValuePair.createText(language);
            return this;
        }

        public Builder setOsVersionNo(String osVersionNo) {
            this.osVersionNo = TypeValuePair.createText(osVersionNo);
            return this;
        }

        public Builder setDeviceId(String deviceId) {
            this.deviceId = TypeValuePair.createText(deviceId);
            return this;
        }

        public Builder setFingerprint(String fingerprint) {
            this.fingerprint = TypeValuePair.createText(fingerprint);
            return this;
        }

        public Builder setOsType(String osType) {
            this.osType = TypeValuePair.createText(osType);
            return this;
        }

        public Builder setApplicationId(String applicationId) {
            this.applicationId = TypeValuePair.createText(applicationId);
            return this;
        }

        public Builder setCompanyNo(String companyNo) {
            this.companyNo = TypeValuePair.createText(companyNo);
            return this;
        }

        public Builder setApplicationVersionNo(String applicationVersionNo) {
            this.applicationVersionNo = TypeValuePair.createText(applicationVersionNo);
            return this;
        }

        public ChallengeSotpRequest build() {
            return new ChallengeSotpRequest(
                    deviceName,
                    applicationTypeCode,
                    logonId,
                    language,
                    osVersionNo,
                    deviceId,
                    fingerprint,
                    osType,
                    applicationId,
                    companyNo,
                    applicationVersionNo);
        }
    }
}
