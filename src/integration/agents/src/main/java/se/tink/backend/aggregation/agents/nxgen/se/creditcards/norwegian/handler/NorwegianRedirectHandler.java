package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.handler;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
            CookieOrigin cookieOrigin =
                    new CookieOrigin(
                            cookie.getHost(), getPort(cookie), cookie.getPath(), cookie.isSecure());
            log.info(
                    "CookieHost: "
                            + cookie.getHost()
                            + " GetPort: "
                            + getPort(cookie)
                            + " GetPath "
                            + cookie.getPath()
                            + " GetSecure : "
                            + cookie.isSecure());

            for (Header header : headers) {
                try {

                    List<Cookie> parse = new BestMatchSpec().parse(header, cookieOrigin);
                    for (Cookie co : parse) {
                        log.info(
                                "Get Cookie Name: "
                                        + co.getName()
                                        + (co.getExpiryDate() != null
                                                ? co.getExpiryDate().toString()
                                                : "No Expiry Date"));
                    }
                    client.addCookie(parse.toArray(new Cookie[0]));
                    Cookie cookie1 = client.getCookies().get(client.getCookies().size() - 1);
                    logStuff(cookie1);
                } catch (MalformedCookieException e) {
                    // NOP
                    log.error("Error: " + e.getMessage());
                }
            }
        }
        return true;
    }

    private int getPort(CookieOrigin host) {

        if (host.getPort() > 0) {
            return host.getPort();
        }
        if (host.isSecure()) {
            return 443;
        }
        throw new IllegalStateException("Illegal schema");
    }

    private void logStuff(Cookie cookie1) {
        log.info(
                "Cookie Name: "
                        + cookie1.getName()
                        + " Get expire date: "
                        + (cookie1.getExpiryDate() != null
                                ? cookie1.getExpiryDate().toString()
                                : "No Expiry Date"));
    }
}
