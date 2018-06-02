package se.tink.backend.product.execution.unit.agents.seb.mortgage;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import se.tink.backend.common.application.mortgage.MortgageProvider;
import se.tink.backend.product.execution.configuration.ProductExecutorConfiguration;

public class JerseyClientWrapper implements HttpClient {
    private final ProductExecutorConfiguration configuration;
    private final Client client;

    @Inject
    public JerseyClientWrapper(Client client, ProductExecutorConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    @Override
    public <T> T get(ApiRequest request, Class<T> responseModel) {
        return createResource(request)
                .get(responseModel);
    }

    @Override
    public <T> T post(ApiRequest request, Class<T> responseModel) {
        return createResource(request)
                .type(MediaType.APPLICATION_JSON + "; charset=utf-8") // SEB requires utf-8 content type
                .post(responseModel, request);
    }

    private WebResource.Builder createResource(ApiRequest request) {
        String uri = createUri(configuration, request);
        WebResource.Builder resource = client.resource(uri)
                .accept(MediaType.APPLICATION_JSON_TYPE);

        // Add all headers from api configuration (Basic auth headers, host headers etc)
        for (Map.Entry<String, String> header : configuration.getSEBMortgageHttpHeaders().entrySet()) {
            resource = resource.header(header.getKey(), header.getValue());
        }

        return resource;
    }

  private static String createUri(ProductExecutorConfiguration configuration, ApiRequest request) {
    return new StringBuilder()
        .append(configuration.getMortgageURI(MortgageProvider.SEB_BANKID))
        .append(request.getUriPath())
        .toString();
  }
}
