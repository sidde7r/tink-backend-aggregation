package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActivationLicenseRequest {
    private final TypeValuePair logonId;
    private final TypeValuePair applicationTypeCode;
    private final TypeValuePair companyNo;
    private final TypeValuePair encryptedClientPublicKeyAndNonce;
    private final TypeValuePair applicationId;
    private final TypeValuePair language;
    private final TypeValuePair applicationVersionNo;
    private final TypeValuePair deviceId;
    private final TypeValuePair clientInitialVector;

    private ActivationLicenseRequest(TypeValuePair logonId, TypeValuePair applicationTypeCode, TypeValuePair companyNo,
            TypeValuePair encryptedClientPublicKeyAndNonce, TypeValuePair applicationId, TypeValuePair language,
            TypeValuePair applicationVersionNo, TypeValuePair deviceId, TypeValuePair clientInitialVector) {
        this.logonId = logonId;
        this.applicationTypeCode = applicationTypeCode;
        this.companyNo = companyNo;
        this.encryptedClientPublicKeyAndNonce = encryptedClientPublicKeyAndNonce;
        this.applicationId = applicationId;
        this.language = language;
        this.applicationVersionNo = applicationVersionNo;
        this.deviceId = deviceId;
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

    public TypeValuePair getEncryptedClientPublicKeyAndNonce() {
        return encryptedClientPublicKeyAndNonce;
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
        private TypeValuePair encryptedClientPublicKeyAndNonce;
        private TypeValuePair applicationId;
        private TypeValuePair language;
        private TypeValuePair applicationVersionNo;
        private TypeValuePair deviceId;
        private TypeValuePair clientInitialVector;

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

        public Builder encryptedClientPublicKeyAndNonce(String encryptedClientPublicKeyAndNonce) {
            this.encryptedClientPublicKeyAndNonce = TypeValuePair.createText(encryptedClientPublicKeyAndNonce);
            return this;
        }

        public Builder applicationId(String applicationId) {
            this.applicationId = TypeValuePair.createText(applicationId);
            return this;
        }

        public Builder language(String language) {
            this.language = TypeValuePair.createText(language);
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

        public Builder clientInitialVector(String clientInitialVector) {
            this.clientInitialVector = TypeValuePair.createText(clientInitialVector);
            return this;
        }

        public ActivationLicenseRequest build() {
            return new ActivationLicenseRequest(logonId, applicationTypeCode, companyNo,
                    encryptedClientPublicKeyAndNonce, applicationId, language, applicationVersionNo, deviceId,
                    clientInitialVector);
        }
    }
}
