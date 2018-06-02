package se.tink.backend.insights.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.common.config.CacheConfiguration;
import se.tink.backend.common.config.CoordinationConfiguration;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.config.DistributedDatabaseConfiguration;
import se.tink.backend.common.config.EmailConfiguration;
import se.tink.libraries.endpoints.EndpointsConfiguration;
import se.tink.backend.common.config.PrometheusConfiguration;
import se.tink.libraries.cluster.Cluster;

public class ActionableInsightsConfiguration extends Configuration {

    @JsonProperty
    private PrometheusConfiguration prometheus = new PrometheusConfiguration();

    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    @JsonProperty
    private DistributedDatabaseConfiguration distributedDatabase = new DistributedDatabaseConfiguration();

    @JsonProperty
    private AuthenticationConfiguration authentication = new AuthenticationConfiguration();

    @JsonProperty
    private CategoryConfiguration category = new SECategories(); // Todo: change if we have different clusters

    @JsonProperty
    private Cluster cluster = Cluster.TINK;

    @JsonProperty
    private CacheConfiguration cache = new CacheConfiguration();

    @JsonProperty
    private CoordinationConfiguration coordination = new CoordinationConfiguration();

    @JsonProperty
    private EmailConfiguration configuration = new EmailConfiguration();

    @JsonProperty
    private EndpointsConfiguration endpoints = new EndpointsConfiguration();

    @JsonProperty
    private boolean isProvidersOnAggregation = false;

    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public PrometheusConfiguration getPrometheus() {
        return prometheus;
    }

    public AuthenticationConfiguration getAuthentication() {
        return authentication;
    }

    public CategoryConfiguration getCategory() {
        return category;
    }

    public DistributedDatabaseConfiguration getDistributedDatabase() {
        return distributedDatabase;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public CoordinationConfiguration getCoordination() {
        return coordination;
    }

    public CacheConfiguration getCache() {
        return cache;
    }

    public EmailConfiguration getEmailConfiguration() {
        return configuration;
    }

    public EndpointsConfiguration getEndpoints() {
        return endpoints;
    }

    public boolean isProvidersOnAggregation() {
        return isProvidersOnAggregation;
    }
}
