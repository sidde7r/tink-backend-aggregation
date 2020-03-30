package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class EndpointSpecification {

    private URL url;
    private Map<String, Object> headers = new HashMap<>();
    private Map<String, String> clientSpecificParams = new HashMap<>();

    public EndpointSpecification(URL url) {
        this.url = url;
    }

    public EndpointSpecification(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public EndpointSpecification withHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public EndpointSpecification withHeaders(Map<String, Object> headers) {
        headers.putAll(headers);
        return this;
    }

    public EndpointSpecification withClientSpecificParameter(
            final String name, final String value) {
        clientSpecificParams.put(name, value);
        return this;
    }

    public Map<String, String> getClientSpecificParameters() {
        return clientSpecificParams;
    }

    public URL getUrl() {
        return url;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }
}
