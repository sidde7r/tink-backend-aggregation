package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.SOFTWARE_ID;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;

public class UkOpenBankingRs256SignatureCreatorTest {

    private UkOpenBankingRs256SignatureCreator ukOpenBankingRs256SignatureCreator;

    @Before
    public void setUp() {
        final JwtSigner jwtSignerMock = mock(JwtSigner.class);
        ukOpenBankingRs256SignatureCreator = new UkOpenBankingRs256SignatureCreator(jwtSignerMock);
    }

    @Test
    public void shouldCreateJwtHeaders() {
        // when
        final Map<String, Object> returned =
                ukOpenBankingRs256SignatureCreator.createJwtHeaders(SOFTWARE_ID);

        // then
        final Set<String> expectedHeaderKeys =
                ImmutableSet.of(
                        JwtHeaders.B64_KEY_HEADER,
                        JwtHeaders.IAT_KEY_HEADER,
                        JwtHeaders.ISS_KEY_HEADER,
                        JwtHeaders.CRIT_KEY_HEADER);
        assertThat(returned.keySet()).containsExactlyElementsOf(expectedHeaderKeys);
    }
}
