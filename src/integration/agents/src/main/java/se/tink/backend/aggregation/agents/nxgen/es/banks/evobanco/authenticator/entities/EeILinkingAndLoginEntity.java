package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeILinkingAndLoginEntity {
    @JsonProperty("nic")
    private String nic;

    @JsonProperty("sistemaOperativo")
    private String operatingSystem;

    @JsonProperty("DatosFirmaOTP")
    private SignatureDataEntity signatureData;

    private String password;

    @JsonProperty("idDispositivo")
    private String idDevice;

    private String idApp;
    private String versionApp;

    @JsonProperty("accesoMovil")
    private String mobileAccess;

    @JsonProperty("modelo")
    private String model;

    @JsonProperty("versionAPI")
    private String versionapi;

    @JsonProperty("codigoEntidad")
    private String entityCode;

    @JsonIgnore
    private EeILinkingAndLoginEntity(Builder builder) {
        nic = builder.nic;
        operatingSystem = builder.operatingSystem;
        signatureData = builder.signatureData;
        password = builder.password;
        idDevice = builder.idDevice;
        idApp = builder.idApp;
        versionApp = builder.versionApp;
        mobileAccess = builder.mobileAccess;
        model = builder.model;
        versionapi = builder.versionapi;
        entityCode = builder.entityCode;
    }

    public static class Builder {
        private String nic;
        private String operatingSystem;
        private SignatureDataEntity signatureData;
        private String password;
        private String idDevice;
        private String idApp;
        private String versionApp;
        private String mobileAccess;
        private String model;
        private String versionapi;
        private String entityCode;

        public EeILinkingAndLoginEntity.Builder withNic(String nic) {
            this.nic = nic;
            return this;
        }

        public EeILinkingAndLoginEntity.Builder withOperatingSystem(String operatingSystem) {
            this.operatingSystem = operatingSystem;
            return this;
        }

        public EeILinkingAndLoginEntity.Builder withSignatureData(
                SignatureDataEntity signatureData) {
            this.signatureData = signatureData;
            return this;
        }

        public EeILinkingAndLoginEntity.Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public EeILinkingAndLoginEntity.Builder withIdDevice(String idDevice) {
            this.idDevice = idDevice;
            return this;
        }

        public EeILinkingAndLoginEntity.Builder withIdApp(String idApp) {
            this.idApp = idApp;
            return this;
        }

        public EeILinkingAndLoginEntity.Builder withVersionApp(String versionApp) {
            this.versionApp = versionApp;
            return this;
        }

        public EeILinkingAndLoginEntity.Builder withMobileAccess(String mobileAccess) {
            this.mobileAccess = mobileAccess;
            return this;
        }

        public EeILinkingAndLoginEntity.Builder withModel(String model) {
            this.model = model;
            return this;
        }

        public EeILinkingAndLoginEntity.Builder withVersionApi(String versionapi) {
            this.versionapi = versionapi;
            return this;
        }

        public EeILinkingAndLoginEntity.Builder withEntityCode(String entityCode) {
            this.entityCode = entityCode;
            return this;
        }

        public EeILinkingAndLoginEntity build() {
            return new EeILinkingAndLoginEntity(this);
        }
    }
}
