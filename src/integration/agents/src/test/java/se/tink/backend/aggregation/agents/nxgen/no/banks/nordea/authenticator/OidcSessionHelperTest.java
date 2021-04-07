package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class OidcSessionHelperTest {

    private static final String TEST_DATA_DIR =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/resources/";

    private static final String CONTINUE_OIDC_AUTH_URL =
            "https://oidc.bankidapis.no/auth/realms/prod/protocol/openid-connect/auth?params...";

    @Test
    public void should_extract_continue_oidc_authentication_url_from_html_response() {
        // given
        String body = readFileContent("oidcSessionActivationResponseWithAuthUrlTag.html");
        HttpResponse httpResponse = mockHttpResponseWithBody(body);

        // when
        String continueAuthUrl = OidcSessionHelper.extractContinueOidcAuthUrl(httpResponse);

        // then
        assertThat(continueAuthUrl).isEqualTo(CONTINUE_OIDC_AUTH_URL);
    }

    @Test
    public void should_throw_illegal_state_exception_when_there_is_no_url_in_html_response() {
        // given
        String body = readFileContent("oidcSessionActivationResponseWithNoAuthUrlTag.html");
        HttpResponse httpResponse = mockHttpResponseWithBody(body);

        // when
        Throwable throwable =
                Assertions.catchThrowable(
                        () -> OidcSessionHelper.extractContinueOidcAuthUrl(httpResponse));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find authentication url in oidc activation response page");
    }

    private static HttpResponse mockHttpResponseWithBody(String body) {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getBody(String.class)).thenReturn(body);
        return response;
    }

    @SneakyThrows
    private String readFileContent(String fileName) {
        Path filePath = Paths.get(TEST_DATA_DIR, fileName);
        return new String(Files.readAllBytes(filePath));
    }
}
