package se.tink.backend.aggregation.nxgen.http.response;

import com.sun.jersey.api.client.ClientResponse;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

public interface HttpResponse {

    HttpRequest getRequest();

    List<URI> getRedirects();

    /**
     * Get the HTTP headers of the response.
     *
     * @return the HTTP headers of the response.
     */
    MultivaluedMap<String, String> getHeaders();

    /**
     * Get the status code.
     *
     * @return the status code.
     */
    int getStatus();

    /**
     * Checks if there is a body available.
     *
     * <p>reset() is called on getEntityInputStream() before check is done. This is because
     * hasEntity() returns false when we receive "exceptions", i.e. http status >= 400 otherwise.
     * Should IOException be thrown during reset(), false is returned.
     *
     * @return true if there is a body present in the response.
     */
    boolean hasBody();

    /**
     * Get the input stream of the response.
     *
     * @return the input stream of the response.
     */
    InputStream getBodyInputStream();

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
    <T> T getBody(Class<T> c) throws HttpClientException, HttpResponseException;

    /**
     * Get the media type of the response.
     *
     * @return the media type.
     */
    MediaType getType();

    /**
     * Get the location.
     *
     * @return the location, otherwise <code>null</code> if not present.
     */
    URI getLocation();

    /**
     * Get the list of cookies.
     *
     * @return the cookies.
     */
    List<NewCookie> getCookies();

    ClientResponse getInternalResponse();
}
