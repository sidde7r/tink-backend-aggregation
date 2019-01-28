package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;

public class ActivationVerificationRequest {
    private final TypeValuePair logonId;
    private final TypeValuePair applicationTypeCode;
    private final TypeValuePair companyNo;
    private final TypeValuePair applicationId;
    private final TypeValuePair language;
    private final TypeValuePair applicationVersionNo;
    private final TypeValuePair deviceId;
    private final TypeValuePair verificationMessage;
    private final TypeValuePair activationMessage;
    private final TypeValuePair osVersionNo;
    private final TypeValuePair osType;
    private final TypeValuePair fingerprint;

    public ActivationVerificationRequest(
            TypeValuePair logonId,
            TypeValuePair applicationTypeCode,
            TypeValuePair companyNo,
            TypeValuePair applicationId,
            TypeValuePair language,
            TypeValuePair applicationVersionNo,
            TypeValuePair deviceId,
            TypeValuePair verificationMessage,
            TypeValuePair activationMessage,
            TypeValuePair osVersionNo,
            TypeValuePair osType,
            TypeValuePair fingerprint) {
        this.logonId = logonId;
        this.applicationTypeCode = applicationTypeCode;
        this.companyNo = companyNo;
        this.applicationId = applicationId;
        this.language = language;
        this.applicationVersionNo = applicationVersionNo;
        this.deviceId = deviceId;
        this.verificationMessage = verificationMessage;
        this.activationMessage = activationMessage;
        this.osVersionNo = osVersionNo;
        this.osType = osType;
        this.fingerprint = fingerprint;
    }

    public TypeValuePair getLogonId() {
        return logonId;
    }

    public TypeValuePair getApplicationTypeCode() {
        return applicationTypeCode;
    }

    public TypeValuePair getCompanyNo() {
        return companyNo;
    }

    public TypeValuePair getApplicationId() {
        return applicationId;
    }

    public TypeValuePair getLanguage() {
        return language;
    }

    public TypeValuePair getApplicationVersionNo() {
        return applicationVersionNo;
    }

    public TypeValuePair getDeviceId() {
        return deviceId;
    }

    public TypeValuePair getVerificationMessage() {
        return verificationMessage;
    }

    public TypeValuePair getActivationMessage() {
        return activationMessage;
    }

    public TypeValuePair getOsVersionNo() {
        return osVersionNo;
    }

    public TypeValuePair getOsType() {
        return osType;
    }

    public TypeValuePair getFingerprint() {
        return fingerprint;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TypeValuePair logonId;
        private TypeValuePair applicationTypeCode;
        private TypeValuePair companyNo;
        private TypeValuePair applicationId;
        private TypeValuePair applicationVersionNo;
        private TypeValuePair deviceId;
        private TypeValuePair verificationMessage;
        private TypeValuePair activationMessage;
        private TypeValuePair osVersionNo;
        private TypeValuePair osType;
        private TypeValuePair fingerprint;
        private TypeValuePair language;

        public Builder logonId(String logonId) {
            this.logonId = TypeValuePair.createText(logonId);
            return this;
        }

        public Builder applicationTypeCode(String applicationTypeCode) {
            this.applicationTypeCode = TypeValuePair.createText(applicationTypeCode);
            return this;
        }

        public Builder companyNo(String companyNo) {
            this.companyNo = TypeValuePair.createText(companyNo);
            return this;
        }

        public Builder applicationId(String applicationId) {
            this.applicationId = TypeValuePair.createText(applicationId);
            return this;
        }

        public Builder applicationVersionNo(String applicationVersionNo) {
            this.applicationVersionNo = TypeValuePair.createText(applicationVersionNo);
            return this;
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = TypeValuePair.createText(deviceId);
            return this;
        }

        public Builder verificationMessage(String verificationMessage) {
            this.verificationMessage = TypeValuePair.createText(verificationMessage);
            return this;
        }

        public Builder activationMessage(String activationMessage) {
            this.activationMessage = TypeValuePair.createText(activationMessage);
            return this;
        }

        public Builder osVersionNo(String osVersionNo) {
            this.osVersionNo = TypeValuePair.createText(osVersionNo);
            return this;
        }

        public Builder osType(String osType) {
            this.osType = TypeValuePair.createText(osType);
            return this;
        }

        public Builder fingerprint(String fingerprint) {
            this.fingerprint = TypeValuePair.createText(fingerprint);
            return this;
        }

        public Builder language(String language) {
            this.language = TypeValuePair.createText(language);
            return this;
        }

        public ActivationVerificationRequest build() {
            return new ActivationVerificationRequest(logonId, applicationTypeCode, companyNo, applicationId,
                    language, applicationVersionNo, deviceId,
                    verificationMessage, activationMessage, osVersionNo, osType, fingerprint);
        }
    }
}
