package se.tink.backend.aggregation.nxgen.http;

import com.sun.jersey.core.header.OutBoundHeaders;
import java.net.URI;
import javax.ws.rs.core.MultivaluedMap;

public class HttpRequestImpl implements HttpRequest {
    private HttpMethod method;
    private URL url;
    private final MultivaluedMap<String, Object> headers;
    private Object body;

    public HttpRequestImpl(HttpMethod method, URL url) {
        this(method, url, null);
    }

    public HttpRequestImpl(HttpMethod method, URL url, Object body) {
        this(method, url, new OutBoundHeaders(), body);
    }

    public HttpRequestImpl(HttpMethod method, URL url, MultivaluedMap<String, Object> headers, Object body) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    @Override
    public URI getURI() {
        return url.toUri();
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }

    @Override
    public Object getBody() {
        return body;
    }

    @Override
    public void setBody(Object body) {
        this.body = body;
    }
}
