package se.tink.backend.integration.tpp_secrets_service.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum CertificatesLocation {
    @JsonAlias({"cluster"})
    CLUSTER("cluster"),
    @JsonAlias({"development-local"})
    DEVELOPMENT_LOCAL("development-local"),
    @JsonAlias({"development-staging"})
    DEVELOPMENT_STAGING("development-staging");


    private final String location;

    CertificatesLocation(String location) {
        this.location = location;
    }

    @JsonCreator
    public static CertificatesLocation fromString(String location) {
        return CertificatesLocation.valueOf(location.toLowerCase());
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return location;
    }
}
