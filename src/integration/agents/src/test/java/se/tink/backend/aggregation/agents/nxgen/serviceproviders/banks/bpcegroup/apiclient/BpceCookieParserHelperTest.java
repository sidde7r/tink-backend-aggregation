package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.Optional;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Before;
import org.junit.Test;

public class BpceCookieParserHelperTest {

    private BpceCookieParserHelper bpceCookieParserHelper;

    @Before
    public void setUp() {
        bpceCookieParserHelper = new BpceCookieParserHelper();
    }

    @Test
    public void testParseValidCookieString() {
        // when
        final Optional<BasicClientCookie> result =
                bpceCookieParserHelper.parseRawCookie("KEY=VALUE; path=/; Httponly;Secure");

        // then
        assertThat(result).isPresent();
        result.ifPresent(
                cookie -> {
                    assertThat(cookie.getName()).isEqualTo("KEY");
                    assertThat(cookie.getValue()).isEqualTo("VALUE");
                    assertThat(cookie.isSecure()).isTrue();
                    assertThat(cookie.getPath()).isEqualTo("/");
                    assertThat(cookie.getAttribute("Httponly")).isEqualTo(null);
                });
    }

    @Test
    public void testParseValidCookieWithExpiryDateString() {
        // when
        final Optional<BasicClientCookie> result =
                bpceCookieParserHelper.parseRawCookie(
                        "CONTEXTE=urlrewkey=; unknown=value; path=/; domain=somedomain; max-age=1200; comment=what");

        // then
        assertThat(result).isPresent();
        result.ifPresent(
                cookie -> {
                    assertThat(cookie.getName()).isEqualTo("CONTEXTE");
                    assertThat(cookie.getValue()).isEqualTo("urlrewkey");
                    assertThat(cookie.isSecure()).isFalse();
                    assertThat(cookie.getPath()).isEqualTo("/");
                    assertThat(cookie.getComment()).isEqualTo("what");
                    assertThat(cookie.getDomain()).isEqualTo("somedomain");
                    assertThat(cookie.getExpiryDate())
                            .isBeforeOrEqualsTo(new Date(System.currentTimeMillis() + 1200));
                });
    }

    @Test
    public void testParseInvalidCookieString() {
        // when
        final Optional<BasicClientCookie> cookie =
                bpceCookieParserHelper.parseRawCookie("KEY=; path=/; Httponly;Secure");

        // then
        assertThat(cookie).isNotPresent();
    }
}
