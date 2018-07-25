package se.tink.libraries.endpoints;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class EndpointsConfiguration extends Configuration {
    @JsonProperty
    private EndpointConfiguration aggregation;
    @JsonProperty
    private EndpointConfiguration analytics;
    @JsonProperty
    private EndpointConfiguration main;
    @JsonProperty
    private EndpointConfiguration api;
    @JsonProperty
    private EndpointConfiguration system;
    @JsonProperty
    private EndpointConfiguration connector;
    @JsonProperty
    private EndpointConfiguration encryption;
    @JsonProperty
    private EndpointConfiguration fasttext = new EndpointConfiguration();
    @JsonProperty
    private EndpointConfiguration categorization = new EndpointConfiguration();
    @JsonProperty
    private EndpointConfiguration oauth;
    @JsonProperty
    private EndpointConfiguration insights;
    @JsonProperty
    private EndpointConfiguration dataExport;
    @JsonProperty
    private EndpointConfiguration aggregationController = new EndpointConfiguration();
    @JsonProperty
    private EndpointConfiguration executor;
    @JsonProperty
    private EndpointConfiguration providerConfiguration = new EndpointConfiguration();

    public EndpointConfiguration getAggregation() {
        return aggregation;
    }

    public EndpointConfiguration getAnalytics() {
        return analytics;
    }

    public EndpointConfiguration getMain() {
        return main;
    }

    public EndpointConfiguration getAPI() {
        return api;
    }

    public EndpointConfiguration getSystem() {
        return system;
    }

    public void setAggregation(EndpointConfiguration aggregation) {
        this.aggregation = aggregation;
    }

    public void setAnalytics(EndpointConfiguration analytics) {
        this.analytics = analytics;
    }

    public void setMain(EndpointConfiguration main) {
        this.main = main;
    }

    public void setAPI(EndpointConfiguration api) {
        this.api = api;
    }

    public void setSystem(EndpointConfiguration system) {
        this.system = system;
    }

    public EndpointConfiguration getConnector() {
        return connector;
    }

    public void setConnector(EndpointConfiguration connector) {
        this.connector = connector;
    }

    public EndpointConfiguration getEncryption() {
        return encryption;
    }

    public void setEncryption(EndpointConfiguration encryption) {
        this.encryption = encryption;
    }

    public EndpointConfiguration getFasttext() {
        return fasttext;
    }

    public void setFasttext(EndpointConfiguration fasttext) {
        this.fasttext = fasttext;
    }

    public EndpointConfiguration getCategorization() {
        return categorization;
    }

    public void setCategorization(EndpointConfiguration categorization) {
        this.categorization = categorization;
    }

    public EndpointConfiguration getOauth() {
        return oauth;
    }

    public EndpointConfiguration getInsights() {
        return insights;
    }

    public EndpointConfiguration getExecutor() { return executor; }

    public EndpointConfiguration getDataExport() {
        return dataExport;
    }

    public EndpointConfiguration getAggregationcontroller() {
        return aggregationController;
    }

    public EndpointConfiguration getProviderConfiguration() { return providerConfiguration; }

    public void setAggregationController(EndpointConfiguration aggregationController) {
        this.aggregationController = aggregationController;
    }
}
