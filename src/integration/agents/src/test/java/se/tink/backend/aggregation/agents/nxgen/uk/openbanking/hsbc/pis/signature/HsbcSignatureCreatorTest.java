package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.pis.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.JwtHeaders;

public class HsbcSignatureCreatorTest {

    private static final String TRUST_ANCHOR_DOMAIN = "dummy.domain";

    private HsbcSignatureCreator hsbcSignatureCreator;

    @Before
    public void setUp() {
        final JwtSigner jwtSignerMock = mock(JwtSigner.class);
        hsbcSignatureCreator = new HsbcSignatureCreator(jwtSignerMock);
        hsbcSignatureCreator.setTrustAnchorDomain(TRUST_ANCHOR_DOMAIN);
    }

    @Test
    public void shouldCreateJwtHeaders() {
        // when
        final Map<String, Object> returned = hsbcSignatureCreator.createJwtHeaders();

        // then
        final Set<String> expectedHeaderKeys =
                ImmutableSet.of(
                        JwtHeaders.IAT_KEY_HEADER,
                        JwtHeaders.ISS_KEY_HEADER,
                        JwtHeaders.TAN_KEY_HEADER,
                        JwtHeaders.CRIT_KEY_HEADER);
        assertThat(returned.keySet()).containsExactlyElementsOf(expectedHeaderKeys);
    }
}
