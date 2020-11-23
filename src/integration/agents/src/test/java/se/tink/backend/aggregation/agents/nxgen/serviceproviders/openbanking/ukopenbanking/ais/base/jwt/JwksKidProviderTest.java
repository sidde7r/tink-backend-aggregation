package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.jwk.JWKSet;
import java.text.ParseException;
import java.util.NoSuchElementException;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.JwksKidProvider;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RunWith(MockitoJUnitRunner.class)
public class JwksKidProviderTest {

    private static final URL JWKS_ENDPOINT = new URL("https://localhost:8888/keys.jwks");

    @Mock private JwksClient jwksClient;

    private JwksKidProvider jwksKidProvider;

    @Before
    public void setUp() throws Exception {

        jwksKidProvider =
                new JwksKidProvider(
                        jwksClient,
                        JWKS_ENDPOINT,
                        CertificateUtils.getX509CertificatesFromBase64EncodedCert(
                                        JwksFixtures.ENCODED_BASE64_CERTIFICATE)
                                .get(0));
    }

    @Test
    public void shouldPickSigningKeyIdFromJWKS() throws ParseException {
        // given
        JWKSet jwkSet = JWKSet.parse(JwksFixtures.JWKS);
        when(jwksClient.get(JWKS_ENDPOINT)).thenReturn(jwkSet);

        // when
        String result = jwksKidProvider.get();

        // then
        assertThat(result, Is.is("319249827807263620565033445582138561170152977440"));
    }

    @Test
    public void shouldThrowsNoSuchElementExceptionWhenPrivateKeyDoesNotHavePairInJWKS()
            throws ParseException {
        String emptyJwks = "{\"keys\":[]}";
        JWKSet jwkSet = JWKSet.parse(emptyJwks);
        when(jwksClient.get(JWKS_ENDPOINT)).thenReturn(jwkSet);

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> jwksKidProvider.get())
                .withNoCause();
    }
}
