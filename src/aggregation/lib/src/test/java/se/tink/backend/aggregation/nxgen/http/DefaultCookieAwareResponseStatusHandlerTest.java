package se.tink.backend.aggregation.nxgen.http;

import com.google.common.collect.Lists;
import java.util.Collections;
import javax.ws.rs.core.NewCookie;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RunWith(MockitoJUnitRunner.class)
public class DefaultCookieAwareResponseStatusHandlerTest {

    private SessionStorage sessionStorage = new SessionStorage();

    @Mock private HttpRequest request;

    @Mock private HttpResponse response;

    private DefaultCookieAwareResponseStatusHandler objectUnderTest =
            new DefaultCookieAwareResponseStatusHandler(sessionStorage);

    @Test
    public void shouldStoreCookiesInTheSessionStorage() {
        // when
        NewCookie newCookie = new NewCookie("cookieName", "cookieValue");
        Mockito.when(response.getCookies()).thenReturn(Lists.newArrayList(newCookie));

        // when
        objectUnderTest.handleResponse(request, response);
        CookieRepository cookieRepository = CookieRepository.getInstance(sessionStorage);

        // then
        Assertions.assertThat(cookieRepository.getCookies()).hasSize(1);
        Assertions.assertThat(cookieRepository.getCookies().get(0).getName())
                .isEqualTo("cookieName");
        Assertions.assertThat(cookieRepository.getCookies().get(0).getValue())
                .isEqualTo("cookieValue");
    }

    @Test
    public void shouldNotStoreCookiesWhenThereIsNoCookiesInResponse() {
        // when
        Mockito.when(response.getCookies()).thenReturn(Collections.emptyList());

        // when
        objectUnderTest.handleResponse(request, response);
        CookieRepository cookieRepository = CookieRepository.getInstance(sessionStorage);

        // then
        Assertions.assertThat(cookieRepository.getCookies()).hasSize(0);
    }
}
