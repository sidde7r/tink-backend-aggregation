package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.mainview;

import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurSessionState;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.CajasurAuthenticationApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RunWith(MockitoJUnitRunner.class)
public class MainViewProcessorTest {

    private SessionStorage sessionStorage = new SessionStorage();

    @Mock private CajasurAuthenticationApiClient cajasurApiClient;

    private MainViewProcessor objectUnderTest;

    @Before
    public void init() {
        objectUnderTest = new MainViewProcessor(cajasurApiClient, sessionStorage);
    }

    @Test
    public void shouldStoreGlobalPositionInTheSessionState() {
        // given
        URL mainPortalPageRedirectUrl = new URL("http://test.domain/path");
        when(cajasurApiClient.submitPostLoginForm(CajasurSessionState.getInstance(sessionStorage)))
                .thenReturn(mainPortalPageRedirectUrl);
        final String mainPageBody = "dummy test main page body";
        when(cajasurApiClient.callForGlobalPositionBody(mainPortalPageRedirectUrl))
                .thenReturn(mainPageBody);

        // when
        AuthenticationStepResponse result = objectUnderTest.process();

        // then
        Assertions.assertThat(CajasurSessionState.getInstance(sessionStorage).getGlobalPosition())
                .isEqualTo(mainPageBody);
        Assertions.assertThat(result.isAuthenticationFinished()).isTrue();
    }
}
