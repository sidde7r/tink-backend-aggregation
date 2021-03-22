package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.handler;

import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.BestMatchSpec;
import org.apache.http.protocol.HttpContext;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.redirect.handler.RedirectHandler;

public class NorwegianRedirectHandler extends RedirectHandler {
    TinkHttpClient client;

    public NorwegianRedirectHandler(TinkHttpClient client) {
        this.client = client;
    }

    @Override
    public boolean allowRedirect(HttpRequest request, HttpResponse response, HttpContext context) {
        CookieOrigin cookie = ((HttpClientContext) context).getCookieOrigin();
        Header[] headers = response.getHeaders("Set-cookie");
        if (headers != null) {
            for (Header header : headers) {
                try {
                    List<Cookie> parse = new BestMatchSpec().parse(header, cookie);
                    client.addCookie(parse.toArray(new Cookie[0]));
                } catch (MalformedCookieException e) {
                    // NOP
                }
            }
        }
        return true;
    }
}
