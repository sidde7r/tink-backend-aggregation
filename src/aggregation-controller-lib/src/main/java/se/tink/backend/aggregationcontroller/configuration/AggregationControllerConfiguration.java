package se.tink.backend.aggregationcontroller.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import se.tink.backend.common.config.CoordinationConfiguration;
import se.tink.backend.common.config.ServiceAuthenticationConfiguration;
import se.tink.libraries.endpoints.EndpointsConfiguration;
import se.tink.backend.common.config.PrometheusConfiguration;

public class AggregationControllerConfiguration extends Configuration {
    @JsonProperty
    private String clusterName = "";

    @JsonProperty
    private String clusterEnvironment = "";

    @JsonProperty()
    private AggregationClusterConfiguration aggregationCluster = new AggregationClusterConfiguration();

    @JsonProperty
    private PrometheusConfiguration prometheus = new PrometheusConfiguration();

    @JsonProperty
    private CoordinationConfiguration coordination = new CoordinationConfiguration();

    @JsonProperty
    private EndpointsConfiguration endpoints = new EndpointsConfiguration();

    @JsonProperty
    private ServiceAuthenticationConfiguration serviceAuthentication = new ServiceAuthenticationConfiguration();

    public PrometheusConfiguration getPrometheus() {
        return prometheus;
    }

    public CoordinationConfiguration getCoordination() {
        return coordination;
    }

    public EndpointsConfiguration getEndpoints() {
        return endpoints;
    }

    public ServiceAuthenticationConfiguration getServiceAuthentication() {
        return serviceAuthentication;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getClusterEnvironment() {
        return clusterEnvironment;
    }

    public AggregationClusterConfiguration getAggregationCluster() {
        return aggregationCluster;
    }
}
