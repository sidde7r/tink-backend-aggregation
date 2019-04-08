package se.tink.backend.aggregation.nxgen.http;

import java.net.URI;
import javax.ws.rs.core.MultivaluedMap;

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
