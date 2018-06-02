package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;

public class FlagsConfiguration {
    @JsonProperty
    private Map<String, Map<String, Double>> register = Maps.newHashMap();
    @JsonProperty
    private Map<String, List<String>> dynamicInheritance = Maps.newHashMap();
    @JsonProperty
    private Map<String, Double> device = Maps.newHashMap();
    @JsonProperty
    private Map<String, List<String>> campaigns = Maps.newHashMap();

    public Map<String, Map<String, Double>> getRegister() {
        return register;
    }

    public Map<String, List<String>> getDynamicInheritance() {
        return dynamicInheritance;
    }

    public Map<String, Double> getDevice() {
        return device;
    }

    public Map<String, List<String>> getCampaigns() {
        return campaigns;
    }

    public void setRegister(Map<String, Map<String, Double>> register) {
        this.register = register;
    }
}
