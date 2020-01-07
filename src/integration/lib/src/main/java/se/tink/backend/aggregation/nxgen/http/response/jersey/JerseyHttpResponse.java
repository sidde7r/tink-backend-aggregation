package se.tink.backend.aggregation.nxgen.http.response.jersey;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.RedirectLocations;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class JerseyHttpResponse implements HttpResponse {
    private final HttpRequest request;
    private final ClientResponse internalResponse;

    public JerseyHttpResponse(HttpRequest request, ClientResponse internalResponse) {
        this.request = request;
        this.internalResponse = internalResponse;

        // `bufferEntity` will read the entity input stream to memory and close it.
        // This is important in order to not leak resources and allow for multiple "open" requests.
        internalResponse.bufferEntity();
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    /*
     * 1. Redirects are populated in Apache's DefaultRedirectStrategy and stored on Apache's HttpContext.
     * 2. The context is accessed and read in TinkApacheHttpClient4Handler and copied to Jersey's response object.
     */
    @Override
    public List<URI> getRedirects() {
        Object o =
                internalResponse
                        .getProperties()
                        .getOrDefault(DefaultRedirectStrategy.REDIRECT_LOCATIONS, null);
        if (o == null || !(o instanceof RedirectLocations)) {
            return new ArrayList<>();
        }
        return ((RedirectLocations) o).getAll();
    }

    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return internalResponse.getHeaders();
    }

    @Override
    public int getStatus() {
        return internalResponse.getStatus();
    }

    @Override
    public boolean hasBody() {
        try {
            internalResponse.getEntityInputStream().reset();
            return internalResponse.hasEntity();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public InputStream getBodyInputStream() {
        return internalResponse.getEntityInputStream();
    }

    @Override
    public <T> T getBody(Class<T> c) throws HttpClientException, HttpResponseException {
        try {
            // Reset the input stream so that we can read multiple times from it.
            internalResponse.getEntityInputStream().reset();
            return internalResponse.getEntity(c);
        } catch (ClientHandlerException | IOException e) {
            throw new HttpClientException(e, request);
        } catch (UniformInterfaceException e) {
            throw new HttpResponseException(e, request, this);
        }
    }

    @Override
    public MediaType getType() {
        return internalResponse.getType();
    }

    @Override
    public URI getLocation() {
        return internalResponse.getLocation();
    }

    @Override
    public List<NewCookie> getCookies() {
        return internalResponse.getCookies();
    }

    @Override
    public ClientResponse getInternalResponse() {
        return internalResponse;
    }
}
