package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;

public class ActivationInstanceRequest {
    private final TypeValuePair logonId;
    private final TypeValuePair applicationTypeCode;
    private final TypeValuePair companyNo;
    private final TypeValuePair deviceCode;
    private final TypeValuePair encryptedServerNonce;
    private final TypeValuePair applicationId;
    private final TypeValuePair language;
    private final TypeValuePair applicationVersionNo;
    private final TypeValuePair deviceId;
    private final TypeValuePair challenge;
    private final TypeValuePair clientInitialVector;

    private ActivationInstanceRequest(
            TypeValuePair logonId,
            TypeValuePair applicationTypeCode,
            TypeValuePair companyNo,
            TypeValuePair deviceCode,
            TypeValuePair encryptedServerNonce,
            TypeValuePair applicationId,
            TypeValuePair language,
            TypeValuePair applicationVersionNo,
            TypeValuePair deviceId,
            TypeValuePair challenge,
            TypeValuePair clientInitialVector) {
        this.logonId = logonId;
        this.applicationTypeCode = applicationTypeCode;
        this.companyNo = companyNo;
        this.deviceCode = deviceCode;
        this.encryptedServerNonce = encryptedServerNonce;
        this.applicationId = applicationId;
        this.language = language;
        this.applicationVersionNo = applicationVersionNo;
        this.deviceId = deviceId;
        this.challenge = challenge;
        this.clientInitialVector = clientInitialVector;
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

    public TypeValuePair getDeviceCode() {
        return deviceCode;
    }

    public TypeValuePair getEncryptedServerNonce() {
        return encryptedServerNonce;
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

    public TypeValuePair getChallenge() {
        return challenge;
    }

    public TypeValuePair getClientInitialVector() {
        return clientInitialVector;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TypeValuePair logonId;
        private TypeValuePair applicationTypeCode;
        private TypeValuePair companyNo;
        private TypeValuePair deviceCode;
        private TypeValuePair encryptedServerNonce;
        private TypeValuePair applicationId;
        private TypeValuePair applicationVersionNo;
        private TypeValuePair deviceId;
        private TypeValuePair challenge;
        private TypeValuePair clientInitialVector;
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

        public Builder deviceCode(String deviceCode) {
            this.deviceCode = TypeValuePair.createText(deviceCode);
            return this;
        }

        public Builder encryptedServerNonce(String encryptedServerNonce) {
            this.encryptedServerNonce = TypeValuePair.createText(encryptedServerNonce);
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

        public Builder challenge(String challenge) {
            this.challenge = TypeValuePair.createText(challenge);
            return this;
        }

        public Builder clientInitialVector(String clientInitialVector) {
            this.clientInitialVector = TypeValuePair.createText(clientInitialVector);
            return this;
        }

        public Builder language(String language) {
            this.language = TypeValuePair.createText(language);
            return this;
        }

        public ActivationInstanceRequest build() {
            return new ActivationInstanceRequest(
                    logonId,
                    applicationTypeCode,
                    companyNo,
                    deviceCode,
                    encryptedServerNonce,
                    applicationId,
                    language,
                    applicationVersionNo,
                    deviceId,
                    challenge,
                    clientInitialVector);
        }
    }
}
