package se.tink.backend.aggregation.nxgen.http.redirect;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;

// The LaxRedirectStrategy automatically redirects all HEAD, GET, POST, and DELETE requests.
public class ApacheHttpRedirectStrategy extends LaxRedirectStrategy {
    private List<RedirectHandler> handlerList = new ArrayList<>();

    public void addHandler(RedirectHandler handler) {
        handlerList.add(handler);
    }

    @Override
    protected URI createLocationURI(String uri) throws ProtocolException {
        for (RedirectHandler handler : handlerList) {
            uri = handler.modifyRedirectUri(uri);
        }
        return super.createLocationURI(uri);
    }

    @Override
    public boolean isRedirected(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
            throws ProtocolException {
        boolean isRedirect = super.isRedirected(httpRequest, httpResponse, httpContext);
        if (!isRedirect) {
            return false;
        }

        for (RedirectHandler handler : handlerList) {
            if (!handler.allowRedirect(httpRequest, httpResponse, httpContext)) {
                return false;
            }
        }
        return true;
    }
}
