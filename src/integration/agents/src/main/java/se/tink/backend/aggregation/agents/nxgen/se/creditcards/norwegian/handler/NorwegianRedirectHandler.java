package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.handler;

import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.RFC2965Spec;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.redirect.handler.RedirectHandler;

public class NorwegianRedirectHandler extends RedirectHandler {
    TinkHttpClient client;
    private static final Logger log = LoggerFactory.getLogger(NorwegianRedirectHandler.class);

    public NorwegianRedirectHandler(TinkHttpClient client) {
        this.client = client;
    }

    @Override
    public boolean allowRedirect(HttpRequest request, HttpResponse response, HttpContext context) {
        Header[] headers = response.getHeaders("Set-cookie");
        if (headers != null) {
            for (Header header : headers) {
                try {

                    List<Cookie> parse =
                            new RFC2965Spec()
                                    .parse(header, ((HttpClientContext) context).getCookieOrigin());
                    client.addCookie(parse.toArray(new Cookie[0]));
                } catch (MalformedCookieException e) {
                    log.error("Error: " + e.getMessage());
                }
            }
        }
        return true;
    }
}
