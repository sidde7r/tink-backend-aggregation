package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeILoginEntity {
    @JsonProperty("nic")
    private String nic;

    @JsonProperty("sistemaOperativo")
    private String operatingSystem;

    private String password;

    @JsonProperty("idDispositivo")
    private String deviceId;

    @JsonProperty("idApp")
    private String appId;

    private String versionApp;

    @JsonProperty("accesoMovil")
    private String mobileAccess;

    @JsonProperty("versionAPI")
    private String apiVersion;

    @JsonProperty("codigoEntidad")
    private String entityCode;

    @JsonIgnore
    private EeILoginEntity(Builder builder) {
        nic = builder.nic;
        operatingSystem = builder.operatingSystem;
        password = builder.password;
        deviceId = builder.deviceId;
        appId = builder.appId;
        versionApp = builder.versionApp;
        mobileAccess = builder.mobileAccess;
        apiVersion = builder.apiVersion;
        entityCode = builder.entityCode;
    }

    public static class Builder {
        private String nic;
        private String operatingSystem;
        private String password;
        private String deviceId;
        private String appId;
        private String versionApp;
        private String mobileAccess;
        private String apiVersion;
        private String entityCode;

        public EeILoginEntity.Builder withNic(String nic) {
            this.nic = nic;
            return this;
        }

        public EeILoginEntity.Builder withOperatingSystem(String operatingSystem) {
            this.operatingSystem = operatingSystem;
            return this;
        }

        public EeILoginEntity.Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public EeILoginEntity.Builder withDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public EeILoginEntity.Builder withAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public EeILoginEntity.Builder withVersionApp(String versionApp) {
            this.versionApp = versionApp;
            return this;
        }

        public EeILoginEntity.Builder withMobileAccess(String mobileAccess) {
            this.mobileAccess = mobileAccess;
            return this;
        }

        public EeILoginEntity.Builder withApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public EeILoginEntity.Builder withEntityCode(String entityCode) {
            this.entityCode = entityCode;
            return this;
        }

        public EeILoginEntity build() {
            return new EeILoginEntity(this);
        }
    }
}
