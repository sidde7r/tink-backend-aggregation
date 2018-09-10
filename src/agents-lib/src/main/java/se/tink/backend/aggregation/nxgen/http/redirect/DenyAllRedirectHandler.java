package se.tink.backend.aggregation.nxgen.http.redirect;

import tink.org.apache.http.HttpRequest;
import tink.org.apache.http.HttpResponse;
import tink.org.apache.http.protocol.HttpContext;

public class DenyAllRedirectHandler extends RedirectHandler {
    @Override
    public boolean allowRedirect(HttpRequest request, HttpResponse response, HttpContext context) {
        return false;
    }
}
