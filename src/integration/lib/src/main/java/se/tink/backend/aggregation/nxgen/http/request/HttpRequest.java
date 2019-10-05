package se.tink.backend.aggregation.nxgen.http.request;

import java.net.URI;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface HttpRequest {
    HttpMethod getMethod();

    void setMethod(HttpMethod method);

    URI getURI();

    URL getUrl();

    void setUrl(URL url);

    MultivaluedMap<String, Object> getHeaders();

    Object getBody();

    void setBody(Object body);
}
