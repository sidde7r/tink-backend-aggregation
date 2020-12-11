package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;

public class UkOpenBankingPs256WithoutBase64SignatureCreatorTest {

    private UkOpenBankingPs256WithoutBase64SignatureCreator
            ukOpenBankingPs256WithoutBase64SignatureCreator;

    @Before
    public void setUp() {
        final JwtSigner jwtSignerMock = mock(JwtSigner.class);
        ukOpenBankingPs256WithoutBase64SignatureCreator =
                new UkOpenBankingPs256WithoutBase64SignatureCreator(jwtSignerMock);
    }

    @Test
    public void shouldCreateJwtHeaders() {
        // when
        final Map<String, Object> returned =
                ukOpenBankingPs256WithoutBase64SignatureCreator.createJwtHeaders();

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
