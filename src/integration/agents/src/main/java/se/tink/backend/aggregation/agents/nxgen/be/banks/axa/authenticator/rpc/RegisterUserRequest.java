package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.DeviceInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.InputEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class RegisterUserRequest {
    private InputEntity input;

    private RegisterUserRequest() {}

    private RegisterUserRequest(InputEntity entity) {
        input = entity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String applcd;
        private String challenge;
        private String clientInitialVector;
        private String deviceBrand;
        private String panNumberFull;
        private String encryptedClientPublicKeyAndNonce;
        private String language;
        private String ucrid;
        private String response;
        private String deviceId;
        private Boolean jailBrokenOrRooted;
        private String model;
        private String operatingSystem;
        private String versionNumber;

        private Builder() {}

        public Builder setResponse(String response) {
            this.response = response;
            return this;
        }

        public Builder setDeviceId(UUID deviceId) {
            this.deviceId = deviceId.toString();
            return this;
        }

        public Builder setJailBrokenOrRooted(boolean jailBrokenOrRooted) {
            this.jailBrokenOrRooted = jailBrokenOrRooted;
            return this;
        }

        public Builder setModel(String model) {
            this.model = model;
            return this;
        }

        public Builder setOperatingSystem(String operatingSystem) {
            this.operatingSystem = operatingSystem;
            return this;
        }

        public Builder setVersionNumber(String versionNumber) {
            this.versionNumber = versionNumber;
            return this;
        }

        public Builder setApplcd(String applcd) {
            this.applcd = applcd;
            return this;
        }

        public Builder setChallenge(String challenge) {
            this.challenge = challenge;
            return this;
        }

        public Builder setClientInitialVector(String clientInitialVector) {
            this.clientInitialVector = clientInitialVector;
            return this;
        }

        public Builder setDeviceBrand(String deviceBrand) {
            this.deviceBrand = deviceBrand;
            return this;
        }

        public Builder setPanNumberFull(String panNumberFull) {
            this.panNumberFull = panNumberFull;
            return this;
        }

        public Builder setEncryptedClientPublicKeyAndNonce(
                String encryptedClientPublicKeyAndNonce) {
            this.encryptedClientPublicKeyAndNonce = encryptedClientPublicKeyAndNonce;
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder setUcrid(String ucrid) {
            this.ucrid = ucrid;
            return this;
        }

        public RegisterUserRequest build() {
            DeviceInfoEntity deviceInfo = new DeviceInfoEntity();
            deviceInfo.setBrand(deviceBrand);
            deviceInfo.setDeviceId(deviceId);
            deviceInfo.setJailBrokenOrRooted(jailBrokenOrRooted);
            deviceInfo.setModel(model);
            deviceInfo.setOperatingSystem(operatingSystem);
            deviceInfo.setVersionNumber(versionNumber);

            InputEntity entity = new InputEntity();
            entity.setApplCd(applcd);
            entity.setChallenge(challenge);
            entity.setClientInitialVector(clientInitialVector);
            entity.setDeviceBrand(deviceBrand);
            entity.setDeviceInfo(deviceInfo);
            entity.setDeviceModel(model);
            entity.setEncryptedClientPublicKeyAndNonce(encryptedClientPublicKeyAndNonce);
            entity.setLanguage(language);
            entity.setPanNumberFull(panNumberFull);
            entity.setResponse(response);
            entity.setuCRid(ucrid);

            return new RegisterUserRequest(entity);
        }
    }
}
