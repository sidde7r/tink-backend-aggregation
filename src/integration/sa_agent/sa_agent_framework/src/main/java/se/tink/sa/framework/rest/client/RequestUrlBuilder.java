package se.tink.sa.framework.rest.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import se.tink.sa.framework.common.exceptions.StandaloneAgentException;

public class RequestUrlBuilder {

    private List<String> urlParts;
    private Map<String, Object> pathVariables;
    private Map<String, Object> uriParameters;

    private RequestUrlBuilder() {
        pathVariables = new HashMap<>();
        uriParameters = new HashMap<>();
        urlParts = new ArrayList<>();
    }

    public RequestUrlBuilder appendUri(String path) {
        if (StringUtils.isEmpty(path)) {
            throw new StandaloneAgentException("Unable to add url part");
        }
        urlParts.add(path);
        return this;
    }

    public RequestUrlBuilder pathVariable(String key, Object value) {
        pathVariables.put(key, value);
        return this;
    }

    public RequestUrlBuilder pathVariables(Map<String, Object> pathVariables) {
        pathVariables.putAll(pathVariables);
        return this;
    }

    public RequestUrlBuilder queryParam(String key, Object value) {
        uriParameters.put(key, value);
        return this;
    }

    public RequestUrlBuilder queryParams(Map<String, Object> pathVariables) {
        uriParameters.putAll(pathVariables);
        return this;
    }

    public static RequestUrlBuilder newInstance() {
        return new RequestUrlBuilder();
    }

    public String build() {
        String url = StringUtils.join(urlParts.toArray());

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
        uriParameters.keySet().forEach(key -> uriBuilder.queryParam(key, uriParameters.get(key)));

        uriBuilder.uriVariables(pathVariables);

        return uriBuilder.toUriString();
    }
}
