package se.tink.backend.export.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.config.CoordinationConfiguration;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.DistributedDatabaseConfiguration;
import se.tink.backend.common.config.PrometheusConfiguration;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.endpoints.EndpointsConfiguration;

public class ExportUserDataConfiguration extends Configuration {

    @JsonProperty
    private PrometheusConfiguration prometheus = new PrometheusConfiguration();

    @JsonProperty
    private CoordinationConfiguration coordination = new CoordinationConfiguration();

    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    @JsonProperty
    private DistributedDatabaseConfiguration distributedDatabase = new DistributedDatabaseConfiguration();

    @JsonProperty
    private EndpointsConfiguration endpoints = new EndpointsConfiguration();

    @JsonProperty
    private CategoryConfiguration category;

    @JsonProperty
    private Cluster cluster;

    @JsonProperty
    private boolean isProvidersOnAggregation = false;

    public CoordinationConfiguration getCoordination() {
        return coordination;
    }

    public EndpointsConfiguration getEndpoints() {
        return endpoints;
    }

    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public DistributedDatabaseConfiguration getDistributedDatabase() {
        return distributedDatabase;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public PrometheusConfiguration getPrometheus() {
        return prometheus;
    }

    public CategoryConfiguration getCategory() {
        return category;
    }

    public boolean isProvidersOnAggregation() {
        return isProvidersOnAggregation;
    }
}

