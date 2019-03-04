package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.InputEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class StoreRegistrationCdRequest {
    private InputEntity input;

    private StoreRegistrationCdRequest() {}

    private StoreRegistrationCdRequest(InputEntity entity) {
        input = entity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String applcd;
        private String language;
        private String clientInitialVector;
        private String derivationCd;
        private String encryptedServerNonce;
        private String serialNo;

        private Builder() {}

        public Builder setApplcd(String applcd) {
            this.applcd = applcd;
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder setClientInitialVector(String clientInitialVector) {
            this.clientInitialVector = clientInitialVector;
            return this;
        }

        public Builder setDerivationCd(String derivationCd) {
            this.derivationCd = derivationCd;
            return this;
        }

        public Builder setEncryptedServerNonce(String encryptedServerNonce) {
            this.encryptedServerNonce = encryptedServerNonce;
            return this;
        }

        public Builder setSerialNo(String serialNo) {
            this.serialNo = serialNo;
            return this;
        }

        public StoreRegistrationCdRequest build() {
            InputEntity entity = new InputEntity();

            entity.setApplCd(applcd);
            entity.setLanguage(language);
            entity.setClientInitialVector(clientInitialVector);
            entity.setDerivationCd(derivationCd);
            entity.setEncryptedServerNonce(encryptedServerNonce);
            entity.setSerialNo(serialNo);

            return new StoreRegistrationCdRequest(entity);
        }
    }
}
