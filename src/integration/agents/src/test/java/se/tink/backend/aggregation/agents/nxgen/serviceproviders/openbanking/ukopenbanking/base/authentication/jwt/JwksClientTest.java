package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authentication.jwt;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.jwk.JWKSet;
import java.text.ParseException;
import org.hamcrest.core.Is;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.JwksClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class JwksClientTest {

    private static final URL JWKS_URL = new URL("https://localhost/keys.jwks");
    private static final String DUMP_JSON = "{\"keys\":[]}";

    @Test
    public void shouldFetchJWKSByHttpClient() throws ParseException {
        // given
        TinkHttpClient httpClient = mock(TinkHttpClient.class);
        JwksClient jwksClient = new JwksClient(httpClient);
        RequestBuilder mockedRequest = mock(RequestBuilder.class);
        when(httpClient.request(JWKS_URL)).thenReturn(mockedRequest);
        when(mockedRequest.get(String.class)).thenReturn(DUMP_JSON);

        // when
        JWKSet result = jwksClient.get(JWKS_URL);

        // then
        assertThat(result.toString(), Is.is(JWKSet.parse(DUMP_JSON).toString()));
    }
}
