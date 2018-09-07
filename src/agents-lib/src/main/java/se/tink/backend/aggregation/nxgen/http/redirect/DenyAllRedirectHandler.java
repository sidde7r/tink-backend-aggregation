package se.tink.backend.aggregation.nxgen.http.redirect;

import se.tink.org.apache.http.HttpRequest;
import se.tink.org.apache.http.HttpResponse;
import se.tink.org.apache.http.protocol.HttpContext;

public class DenyAllRedirectHandler extends RedirectHandler {
    @Override
    public boolean allowRedirect(HttpRequest request, HttpResponse response, HttpContext context) {
        return false;
    }
}
