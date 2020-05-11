package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator;

import lombok.AllArgsConstructor;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AllArgsConstructor
public class CookieManager {
    private final WebDriver driver;
    private final TinkHttpClient client;

    public void setCookiesToClient() {
        driver.manage().getCookies().forEach(cookie -> client.addCookie(toTinkCookie(cookie)));
    }

    private BasicClientCookie toTinkCookie(final Cookie cookie) {
        BasicClientCookie clientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
        clientCookie.setDomain(cookie.getDomain());
        clientCookie.setExpiryDate(cookie.getExpiry());
        clientCookie.setPath(cookie.getPath());
        clientCookie.setSecure(cookie.isSecure());
        return clientCookie;
    }
}
