package se.tink.backend.aggregation.nxgen.http.redirect;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import se.tink.backend.aggregation.nxgen.http.redirect.handler.RedirectHandler;

public class DenyAllRedirectHandler extends RedirectHandler {
    @Override
    public boolean allowRedirect(HttpRequest request, HttpResponse response, HttpContext context) {
        return false;
    }
}
