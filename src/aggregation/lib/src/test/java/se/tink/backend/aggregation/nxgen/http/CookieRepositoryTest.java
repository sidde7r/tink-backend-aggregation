package se.tink.backend.aggregation.nxgen.http;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CookieRepositoryTest {

    private SessionStorage sessionStorage = new SessionStorage();

    @Test
    public void shouldStoreCookie() {
        // given
        CookieRepository objectUnderTest = CookieRepository.getInstance(sessionStorage);

        // when
        objectUnderTest.addCookie("cookieName", "cookieValue", "cookiePath", "cookieDomain", 2);
        objectUnderTest.save(sessionStorage);
        Optional<CookieRepository> result =
                sessionStorage.get(
                        sessionStorage.keySet().iterator().next(), CookieRepository.class);

        // then
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getCookies()).hasSize(1);
        Assertions.assertThat(result.get().getCookies().get(0).getName()).isEqualTo("cookieName");
        Assertions.assertThat(result.get().getCookies().get(0).getValue()).isEqualTo("cookieValue");
        Assertions.assertThat(result.get().getCookies().get(0).getPath()).isEqualTo("cookiePath");
        Assertions.assertThat(result.get().getCookies().get(0).getDomain())
                .isEqualTo("cookieDomain");
        Assertions.assertThat(result.get().getCookies().get(0).getVersion()).isEqualTo(2);
    }
}
