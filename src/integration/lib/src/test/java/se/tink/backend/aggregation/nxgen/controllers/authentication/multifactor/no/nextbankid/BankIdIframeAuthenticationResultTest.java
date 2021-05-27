package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.Date;
import java.util.List;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ResponseFromProxy;

public class BankIdIframeAuthenticationResultTest {

    // selenium cookies expiry date is rounded to full seconds so we cannot use nanos in test
    private static final long NANOS_IN_SECOND = 1_000_000;

    @Test
    public void should_convert_selenium_cookies_to_apache_cookies() {
        // given
        BankIdWebDriver driver = mock(BankIdWebDriver.class);
        when(driver.getCookies())
                .thenReturn(
                        ImmutableSet.of(
                                new org.openqa.selenium.Cookie.Builder(
                                                "cookieName1", "cookieValue1")
                                        .domain("domain1")
                                        .path("path1")
                                        .expiresOn(new Date(NANOS_IN_SECOND))
                                        .isSecure(true)
                                        .build(),
                                new org.openqa.selenium.Cookie.Builder(
                                                "cookieName2", "cookieValue2")
                                        .domain("domain2")
                                        .path("path2")
                                        .expiresOn(new Date(2 * NANOS_IN_SECOND))
                                        .isSecure(false)
                                        .build()));

        BankIdIframeAuthenticationResult authenticationResult =
                new BankIdIframeAuthenticationResult(mock(ResponseFromProxy.class), driver);

        // when
        List<Cookie> cookies = authenticationResult.getCookies();

        // then
        assertThat(cookies.size()).isEqualTo(2);

        Cookie cookie1 = getCookieByName(cookies, "cookieName1");
        assertThat(cookie1)
                .isEqualToComparingFieldByFieldRecursively(
                        new ApacheCookieBuilder()
                                .name("cookieName1")
                                .value("cookieValue1")
                                .domain("domain1")
                                .path("path1")
                                .expiry(new Date(NANOS_IN_SECOND))
                                .isSecure(true)
                                .build());

        Cookie cookie2 = getCookieByName(cookies, "cookieName2");
        assertThat(cookie2)
                .isEqualToComparingFieldByFieldRecursively(
                        new ApacheCookieBuilder()
                                .name("cookieName2")
                                .value("cookieValue2")
                                .domain("domain2")
                                .path("path2")
                                .expiry(new Date(2 * NANOS_IN_SECOND))
                                .isSecure(false)
                                .build());
    }

    private static Cookie getCookieByName(List<Cookie> cookies, String cookieName) {
        return cookies.stream()
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot find cookie by given name"));
    }

    @Setter
    @Accessors(fluent = true)
    private static class ApacheCookieBuilder {

        private String name;
        private String value;
        private String domain;
        private String path;
        private Date expiry;
        private boolean isSecure;

        Cookie build() {
            BasicClientCookie cookie = new BasicClientCookie(name, value);
            cookie.setDomain(domain);
            cookie.setPath(path);
            cookie.setExpiryDate(expiry);
            cookie.setSecure(isSecure);
            return cookie;
        }
    }
}
