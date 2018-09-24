package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown =  true)
public class S3StorageConfiguration {
    @JsonProperty
    private boolean enabled = false;

    @JsonProperty
    private String url;

    @JsonProperty
    private String region;

    @JsonProperty
    private String agentDebugBucketName;

    public S3StorageConfiguration() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAgentDebugBucketName(){
        return agentDebugBucketName;
    }

    public void setAgentDebugBucketName(String agentDebugBucketName){
        this.agentDebugBucketName = agentDebugBucketName;
    }
}
