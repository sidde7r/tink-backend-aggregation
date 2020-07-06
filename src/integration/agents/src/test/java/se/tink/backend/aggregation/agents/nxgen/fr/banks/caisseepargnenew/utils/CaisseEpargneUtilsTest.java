package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Test;

public class CaisseEpargneUtilsTest {
    @Test
    public void testParseValidCookieString() throws ParseException {
        Optional<BasicClientCookie> cookie =
                CaisseEpargneUtils.parseRawCookie("KEY=VALUE; path=/; Httponly;Secure");
        assertThat(cookie).isPresent();
        assertThat(cookie.get().getName()).isEqualTo("KEY");
        assertThat(cookie.get().getValue()).isEqualTo("VALUE");
        assertThat(cookie.get().isSecure()).isTrue();
        assertThat(cookie.get().getPath()).isEqualTo("/");
        assertThat(cookie.get().getAttribute("Httponly")).isEqualTo(null);

        cookie =
                CaisseEpargneUtils.parseRawCookie(
                        "CONTEXTE=urlrewkey=; unknown=value; path=/; domain=somedomain; max-age=1200; comment=what");
        assertThat(cookie).isPresent();
        assertThat(cookie.get().getName()).isEqualTo("CONTEXTE");
        assertThat(cookie.get().getValue()).isEqualTo("urlrewkey");
        assertThat(cookie.get().isSecure()).isFalse();
        assertThat(cookie.get().getPath()).isEqualTo("/");
        assertThat(cookie.get().getComment()).isEqualTo("what");
        assertThat(cookie.get().getDomain()).isEqualTo("somedomain");
        assertThat(cookie.get().getExpiryDate())
                .isBeforeOrEqualsTo(new Date(System.currentTimeMillis() + 1200));
    }

    @Test
    public void testParseInvalidCookieString() {
        Optional<BasicClientCookie> cookie =
                CaisseEpargneUtils.parseRawCookie("KEY=; path=/; Httponly;Secure");
        assertThat(cookie).isNotPresent();
    }
}
