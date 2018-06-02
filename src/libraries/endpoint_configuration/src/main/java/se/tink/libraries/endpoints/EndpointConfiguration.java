package se.tink.libraries.endpoints;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class EndpointConfiguration {
    @JsonProperty
    private String url;
    @JsonProperty
    private String accessToken;
    @JsonProperty
    private List<String> pinnedCertificates;

    public String getAccessToken() {
        return accessToken;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getPinnedCertificates() {
        return pinnedCertificates;
    }
}
