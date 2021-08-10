package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.apache.http.cookie.Cookie;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationResult;

public class DnbAuthenticatorTest {

    /*
    Mocks
     */
    private DnbApiClient apiClient;

    /*
    Real
     */
    private DnbAuthenticator dnbAuthenticator;

    @Before
    public void setup() {
        apiClient = mock(DnbApiClient.class);

        dnbAuthenticator = new DnbAuthenticator(apiClient);
    }

    @Test
    public void should_return_correct_authentication_finish_url() {
        // when
        String url = dnbAuthenticator.getSubstringOfUrlIndicatingAuthenticationFinish();

        // then
        assertThat(url).isEqualTo(DnbConstants.Url.FINISH_LOGIN);
    }

    @Test
    public void should_add_cookies_to_api_client() {
        // given
        List<Cookie> webDriverCookies = ImmutableList.of(mock(Cookie.class), mock(Cookie.class));

        BankIdIframeAuthenticationResult authenticationResult =
                mock(BankIdIframeAuthenticationResult.class);
        when(authenticationResult.getCookies()).thenReturn(webDriverCookies);

        // when
        dnbAuthenticator.handleBankIdAuthenticationResult(authenticationResult);

        // then
        ArgumentCaptor<List<Cookie>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiClient).addCookies(captor.capture());
        List<Cookie> cookiesSet = captor.getValue();

        assertThat(cookiesSet).isEqualTo(webDriverCookies);
    }
}
