package se.tink.backend.aggregation.nxgen.http.redirect;

// This redirect strategy fixes illegal redirect urls so that our client wont crash.
public class FixRedirectHandler extends RedirectHandler {
    @Override
    public String modifyRedirectUri(String uri) {
        return uri.replaceAll(" ", "%20");
    }
}
