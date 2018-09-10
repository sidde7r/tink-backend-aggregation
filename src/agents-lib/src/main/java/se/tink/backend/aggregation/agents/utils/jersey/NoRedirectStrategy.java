package se.tink.backend.aggregation.agents.utils.jersey;

import tink.org.apache.http.HttpRequest;
import tink.org.apache.http.HttpResponse;
import tink.org.apache.http.ProtocolException;
import tink.org.apache.http.client.RedirectStrategy;
import tink.org.apache.http.client.methods.HttpUriRequest;
import tink.org.apache.http.protocol.HttpContext;

public class NoRedirectStrategy implements RedirectStrategy {
    @Override
    public boolean isRedirected(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws ProtocolException {
        // `false` == Don't redirect anything
        return false;
    }

    @Override
    public HttpUriRequest getRedirect(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws ProtocolException {
        return null;
    }
}
