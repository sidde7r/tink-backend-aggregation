package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BpceTokenExtractorTest {

    private BpceTokenExtractor bpceTokenExtractor;

    @Before
    public void setUp() {
        bpceTokenExtractor = new BpceTokenExtractor();
    }

    @Test
    public void testTokenExtractionWithValidLocation() throws URISyntaxException {
        // given
        final HttpResponse httpResponseMock =
                createHttpResponseMock(
                        "containerApp://BAPIStepUpSuccess#access_token=vqqgboKxVY7fXxhXi9tJF7EgGeXi1P1g1D4CTWdlec1z5b880OkZxB&token_type=Bearer&expires_in=296&id_token=eyJ4NXQjUzI1NiI6Ii0wcWxhcnBONDlOUUpmQmNReXpaLTZiXzdHRW1LTkdMS24wVEVrcnU5MzAiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdF9oYXNoIjoieDlCNTMwQzJ6dmdqQkh5NldaWkhUdyIsInN1YiI6Ijc1NjI3MzcyNSIsImF1ZCI6WyJodHRwczovL3d3dy5hcy1leC1hdGgtZ3JvdXBlLmNhaXNzZS1lcGFyZ25lLmZyL2FwaS9vYXV0aC92Mi9hdXRob3JpemUiLCJmNGVlMjE0NC0wZDY4LTRiOTAtYWU3OC0yNWUyNTVhMWYzYWMiXSwibGFzdF9sb2dpbiI6IjIwMjAtMDctMDNUMTE6Mzg6NDUuODcwWiIsImF6cCI6ImY0ZWUyMTQ0LTBkNjgtNGI5MC1hZTc4LTI1ZTI1NWExZjNhYyIsImF1dGhfdGltZSI6MTU5Mzc3NzM0MiwiaXNzIjoiaHR0cHM6Ly93d3cuYXMtZXgtYXRoLWdyb3VwZS5jYWlzc2UtZXBhcmduZS5mci9hcGkvb2F1dGgvdjIvYXV0aG9yaXplIiwiZXhwIjoxNTkzNzc3NjQyLCJub25jZSI6IkREM0MwRkFGLTUwNTktNEYxNi04QkUxLTdDOUNDQzE4MURBNSIsImlhdCI6MTU5Mzc3NzM0Mn0.ssve8xiBQ3IzMERUSddYS61epnE0UBFrGFa-I6yhTw4dKkLZ6hi2dpDMXsCStjti2Y9yFjVHRm7GWVkaD9EU60bZ89e_ZA0DLDlyAXGCp7l0iz47x4s0H1_0oK-sanBaiUTflb0Jxu-acKTYfjz-9ReEy-RnIuAKrpFknnEu4SVir6ekeBZi9oIv0-rPS_igbXOVXI8NiSLN_L4sDfMW0f2fv-FLZCDvR2zoL2EjGPyjVEY2_i1q1Ny1h29eH7CzbKZxxFD7z2bhuLHM81-E-nKHBbHIiRgfFNDJQu_HQPuT_OM4NUwpevFDG-_JwmBcYGvPv0WirYAsMG_bZxLrFA");

        // when
        final OAuth2Token returnedToken = bpceTokenExtractor.extractToken(httpResponseMock);

        // then
        assertThat(returnedToken.getAccessToken())
                .isEqualTo("vqqgboKxVY7fXxhXi9tJF7EgGeXi1P1g1D4CTWdlec1z5b880OkZxB");
        assertThat(returnedToken.getExpiresInSeconds()).isEqualTo(296);
        assertThat(returnedToken.getTokenType()).isEqualTo("Bearer");
        assertThat(returnedToken.getOptionalRefreshToken().isPresent()).isFalse();
    }

    @Test
    public void testTokenExtractionWithInvalidLocation() throws URISyntaxException {
        // given
        final HttpResponse httpResponseMock = createHttpResponseMock("thisshouldnotwork");

        // when
        final OAuth2Token returnedToken = bpceTokenExtractor.extractToken(httpResponseMock);

        // then
        assertThat(returnedToken.getAccessToken()).isEqualTo(null);
        assertThat(returnedToken.getExpiresInSeconds()).isEqualTo(0);
        assertThat(returnedToken.getTokenType()).isEqualTo(null);
        assertThat(returnedToken.getOptionalRefreshToken().isPresent()).isFalse();
    }

    private HttpResponse createHttpResponseMock(String uri) throws URISyntaxException {
        final HttpResponse httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.getLocation()).thenReturn(new URI(uri));

        return httpResponseMock;
    }
}
