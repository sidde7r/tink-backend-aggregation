package se.tink.backend.aggregation.nxgen.http;

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
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class HttpResponse {
    private final HttpRequest request;
    private final ClientResponse internalResponse;

    public HttpResponse(HttpRequest request, ClientResponse internalResponse) {
        this.request = request;
        this.internalResponse = internalResponse;

        // `bufferEntity` will read the entity input stream to memory and close it.
        // This is important in order to not leak resources and allow for multiple "open" requests.
        internalResponse.bufferEntity();
    }

    public HttpRequest getRequest() {
        return request;
    }

    /*
     * 1. Redirects are populated in Apache's DefaultRedirectStrategy and stored on Apache's HttpContext.
     * 2. The context is accessed and read in TinkApacheHttpClient4Handler and copied to Jersey's response object.
     */
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

    /**
     * Get the HTTP headers of the response.
     *
     * @return the HTTP headers of the response.
     */
    public MultivaluedMap<String, String> getHeaders() {
        return internalResponse.getHeaders();
    }

    /**
     * Get the status code.
     *
     * @return the status code.
     */
    public int getStatus() {
        return internalResponse.getStatus();
    }

    /**
     * Checks if there is a body available.
     *
     * <p>reset() is called on getEntityInputStream() before check is done. This is because
     * hasEntity() returns false when we receive "exceptions", i.e. http status >= 400 otherwise.
     * Should IOException be thrown during reset(), false is returned.
     *
     * @return true if there is a body present in the response.
     */
    public boolean hasBody() {
        try {
            internalResponse.getEntityInputStream().reset();
            return internalResponse.hasEntity();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get the input stream of the response.
     *
     * @return the input stream of the response.
     */
    public InputStream getBodyInputStream() {
        return internalResponse.getEntityInputStream();
    }

    /**
     * Get the body of the response.
     *
     * <p>If the body is not an instance of Closeable then the body input stream is closed.
     *
     * @param <T> the type of the response.
     * @param c the type of the body.
     * @return an instance of the type <code>c</code>.
     * @throws HttpClientException if there is an error processing the response.
     * @throws HttpResponseException if the response status is 204 (No Content).
     */
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

    /**
     * Get the media type of the response.
     *
     * @return the media type.
     */
    public MediaType getType() {
        return internalResponse.getType();
    }

    /**
     * Get the location.
     *
     * @return the location, otherwise <code>null</code> if not present.
     */
    public URI getLocation() {
        return internalResponse.getLocation();
    }

    /**
     * Get the language.
     *
     * @return the language, otherwise <code>null</code> if not present.
     */
    public String getLanguage() {
        return internalResponse.getLanguage();
    }

    /**
     * Get the list of cookies.
     *
     * @return the cookies.
     */
    public List<NewCookie> getCookies() {
        return internalResponse.getCookies();
    }
}
