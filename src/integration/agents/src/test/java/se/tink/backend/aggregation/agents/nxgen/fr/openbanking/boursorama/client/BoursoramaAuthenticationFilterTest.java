package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BoursoramaAuthenticationFilterTest {

    private static final String TOKEN_VALUE = "TOKENTOKEN";
    private BoursoramaAuthenticationFilter boursoramaAuthenticationFilter;

    @Before
    public void setUp() throws Exception {
        OAuth2Token token = OAuth2Token.createBearer(TOKEN_VALUE, "refresh_token", 12345);

        boursoramaAuthenticationFilter = new BoursoramaAuthenticationFilter();
        boursoramaAuthenticationFilter.setTokenToUse(token);
        boursoramaAuthenticationFilter.setNext(Mockito.mock(Filter.class));
    }

    @Test
    public void handle() {
        // given
        HttpRequestImpl request = new HttpRequestImpl(HttpMethod.GET, new URL("dummy.url"));

        // when
        boursoramaAuthenticationFilter.handle(request);

        // then
        Assertions.assertThat(request.getHeaders().getFirst("Authorization"))
                .isEqualTo("Bearer " + TOKEN_VALUE);
    }
}
