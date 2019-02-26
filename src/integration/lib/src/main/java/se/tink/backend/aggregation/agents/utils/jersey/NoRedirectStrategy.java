package se.tink.backend.aggregation.agents.utils.jersey;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

public class NoRedirectStrategy implements RedirectStrategy {
    @Override
    public boolean isRedirected(
            HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws ProtocolException {
        // `false` == Don't redirect anything
        return false;
    }

    @Override
    public HttpUriRequest getRedirect(
            HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws ProtocolException {
        return null;
    }
}
