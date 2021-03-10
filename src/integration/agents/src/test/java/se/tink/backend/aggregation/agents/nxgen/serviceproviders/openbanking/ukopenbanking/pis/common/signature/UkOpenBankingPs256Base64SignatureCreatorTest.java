package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createSoftwareStatementAssertion;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;

public class UkOpenBankingPs256Base64SignatureCreatorTest {

    private UkOpenBankingPs256Base64SignatureCreator ukOpenBankingPs256Base64SignatureCreator;

    @Before
    public void setUp() {
        final SoftwareStatementAssertion softwareStatementMock = createSoftwareStatementAssertion();
        final JwtSigner jwtSignerMock = mock(JwtSigner.class);
        ukOpenBankingPs256Base64SignatureCreator =
                new UkOpenBankingPs256Base64SignatureCreator(jwtSignerMock);
        ukOpenBankingPs256Base64SignatureCreator.setSoftwareStatement(softwareStatementMock);
    }

    @Test
    public void shouldCreateJwtHeaders() {
        // when
        final Map<String, Object> returned =
                ukOpenBankingPs256Base64SignatureCreator.createJwtHeaders();

        // then
        final Set<String> expectedHeaderKeys =
                ImmutableSet.of(
                        JwtHeaders.B64_KEY_HEADER,
                        JwtHeaders.IAT_KEY_HEADER,
                        JwtHeaders.ISS_KEY_HEADER,
                        JwtHeaders.TAN_KEY_HEADER,
                        JwtHeaders.CRIT_KEY_HEADER);
        assertThat(returned.keySet()).containsExactlyElementsOf(expectedHeaderKeys);
    }
}
