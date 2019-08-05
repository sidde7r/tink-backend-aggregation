package se.tink.backend.integration.tpp_secrets_service.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum CertificatesLocation {
    @JsonAlias({"cluster"})
    CLUSTER("cluster"),
    @JsonAlias({"home-pem"})
    HOME_PEM("home-pem"),
    @JsonAlias({"home-p12"})
    HOME_P12("home-p12");

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
