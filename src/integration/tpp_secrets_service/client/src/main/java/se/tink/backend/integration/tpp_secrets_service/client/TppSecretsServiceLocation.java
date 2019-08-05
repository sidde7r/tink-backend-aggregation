package se.tink.backend.integration.tpp_secrets_service.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum TppSecretsServiceLocation {
    @JsonAlias({"inside-cluster"})
    INSIDE_CLUSTER("inside-cluster"),
    @JsonAlias({"outside-cluster-local"})
    OUTSIDE_CLUSTER_LOCAL("outside-cluster-local"),
    @JsonAlias({"outside-cluster-staging"})
    OUTSIDE_CLUSTER_STAGING("outside-cluster-staging"),
    @JsonAlias({"not-available"})
    NOT_AVAILABLE("not-available");

    private final String location;

    TppSecretsServiceLocation(String location) {
        this.location = location;
    }

    @JsonCreator
    public static TppSecretsServiceLocation fromString(String location) {
        return TppSecretsServiceLocation.valueOf(location.toLowerCase());
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return location;
    }
}
