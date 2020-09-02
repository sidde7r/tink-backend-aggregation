package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.macgenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.CLIENT_SECRET;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.MAC_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.MAC_SIGNATURE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.MILLIS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.NONCE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.RESOURCE_PATH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createAuthMacValue;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createBaseAuthString;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createBaseDataString;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createDataMacValue;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createHmacToken;

import java.time.Clock;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexGrantType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.configuration.AmexConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;

public class AmexMacGeneratorTest {

    private AmexMacGenerator amexMacGenerator;

    private MacSignatureCreator macSignatureCreatorMock;

    @Before
    public void setUp() {
        final AmexConfiguration amexConfigurationMock = mock(AmexConfiguration.class);
        when(amexConfigurationMock.getClientId()).thenReturn(CLIENT_ID);
        when(amexConfigurationMock.getClientSecret()).thenReturn(CLIENT_SECRET);

        macSignatureCreatorMock = mock(MacSignatureCreator.class);
        when(macSignatureCreatorMock.createNonce()).thenReturn(NONCE);

        final Clock clockMock = mock(Clock.class);
        when(clockMock.millis()).thenReturn(MILLIS);

        amexMacGenerator =
                new AmexMacGenerator(amexConfigurationMock, macSignatureCreatorMock, clockMock);
    }

    @Test
    public void shouldGenerateAuthMacValue() {
        // given
        final String expectedBaseAuthString = createBaseAuthString();
        when(macSignatureCreatorMock.createSignature(CLIENT_SECRET, expectedBaseAuthString))
                .thenReturn(MAC_SIGNATURE);

        // when
        final String resultMacValue =
                amexMacGenerator.generateAuthMacValue(AmexGrantType.AUTHORIZATION_CODE);

        // then
        final String expectedAuthMacValue = createAuthMacValue();
        assertThat(resultMacValue).isEqualTo(expectedAuthMacValue);
    }

    @Test
    public void shouldGenerateDataMacValue() {
        // given
        final String expectedBaseDataString = createBaseDataString();
        when(macSignatureCreatorMock.createSignature(MAC_KEY, expectedBaseDataString))
                .thenReturn(MAC_SIGNATURE);

        final HmacToken hmacToken = createHmacToken();

        // when
        final String resultMacValue =
                amexMacGenerator.generateDataMacValue(RESOURCE_PATH, hmacToken);

        // then
        final String expectedDataMacValue = createDataMacValue();
        assertThat(resultMacValue).isEqualTo(expectedDataMacValue);
    }
}
