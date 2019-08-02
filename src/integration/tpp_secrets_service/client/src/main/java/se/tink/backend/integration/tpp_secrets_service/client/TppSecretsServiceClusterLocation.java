package se.tink.backend.integration.tpp_secrets_service.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum TppSecretsServiceClusterLocation {
    @JsonAlias({"within-cluster"})
    WITHIN_CLUSTER("within-cluster"),
    @JsonAlias({"outside-cluster-local"})
    OUTSIDE_CLUSTER_LOCAL("outside-cluster-local"),
    @JsonAlias({"outside-cluster-staging"})
    OUTSIDE_CLUSTER_STAGING("outside-cluster-staging"),
    @JsonAlias({"not-available"})
    NOT_AVAILABLE("not-available");

    private final String location;

    TppSecretsServiceClusterLocation(String location) {
        this.location = location;
    }

    @JsonCreator
    public static TppSecretsServiceClusterLocation fromString(String location) {
        return TppSecretsServiceClusterLocation.valueOf(location.toLowerCase());
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return location;
    }
}
