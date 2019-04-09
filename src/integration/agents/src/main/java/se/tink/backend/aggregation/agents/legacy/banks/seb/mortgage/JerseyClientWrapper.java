package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;

public class JerseyClientWrapper implements HttpClient {
    private final ApiConfiguration apiConfiguration;
    private final Client client;

    @Inject
    public JerseyClientWrapper(Client client, ApiConfiguration apiConfiguration) {
        this.client = client;
        this.apiConfiguration = apiConfiguration;
    }

    @Override
    public <T> T get(ApiRequest request, Class<T> responseModel) {
        return createResource(request).get(responseModel);
    }

    @Override
    public <T> T post(ApiRequest request, Class<T> responseModel) {
        return createResource(request)
                .type(
                        MediaType.APPLICATION_JSON
                                + "; charset=utf-8") // SEB requires utf-8 content type
                .post(responseModel, request);
    }

    private WebResource.Builder createResource(ApiRequest request) {
        String uri = createUri(apiConfiguration, request);
        WebResource.Builder resource = client.resource(uri).accept(MediaType.APPLICATION_JSON_TYPE);

        // Add all headers from api configuration (Basic auth headers, host headers etc)
        for (Map.Entry<String, String> header : apiConfiguration.getHeaders().entrySet()) {
            resource = resource.header(header.getKey(), header.getValue());
        }

        return resource;
    }

    private static String createUri(ApiConfiguration apiConfiguration, ApiRequest request) {
        return new StringBuilder()
                .append(apiConfiguration.isHttps() ? "https://" : "http://")
                .append(apiConfiguration.getBaseUrl())
                .append(request.getUriPath())
                .toString();
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(client);
    }
}
