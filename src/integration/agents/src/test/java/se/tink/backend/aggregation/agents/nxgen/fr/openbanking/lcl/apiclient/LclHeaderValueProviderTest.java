package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.DATE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.DIGEST;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.NOW;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.QSEALC_KEY_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.REQUEST_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.SIGNATURE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.ZONE_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createAgentConfigurationMock;

import java.time.Clock;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.signature.LclSignatureProvider;

public class LclHeaderValueProviderTest {

    private LclHeaderValueProvider lclHeaderValueProvider;

    @Before
    public void setUp() {
        final LclSignatureProvider signatureProviderMock = createLclSignatureProviderMock();
        final LclConfiguration configurationMock =
                createAgentConfigurationMock().getProviderSpecificConfiguration();
        final Clock clockMock = createClockMock();

        lclHeaderValueProvider =
                new LclHeaderValueProvider(signatureProviderMock, configurationMock, clockMock);
    }

    @Test
    public void shouldGetSignatureHeaderValue() {
        // when
        final String returnedResponse =
                lclHeaderValueProvider.getSignatureHeaderValue(REQUEST_ID, DATE, DIGEST);

        // then
        final String expectedResponse =
                String.format(
                        "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"x-request-id date digest\",signature=\"%s\"",
                        QSEALC_KEY_ID, SIGNATURE);

        assertThat(returnedResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldGetDigestHeaderValueForNonNullBody() {
        // when
        final RefreshTokenRequest requestBody = new RefreshTokenRequest(CLIENT_ID, REFRESH_TOKEN);
        final String returnedResponse = lclHeaderValueProvider.getDigestHeaderValue(requestBody);

        // then
        final String expectedResponse = "SHA-256=RGpM4Lo75KJEw1qbUbD1AR39R3R7NsA3VLid9WXaYf4=";
        assertThat(returnedResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldGetDigestHeaderValueForNullBody() {
        // when
        final String returnedResponse = lclHeaderValueProvider.getDigestHeaderValue(null);

        // then
        final String expectedResponse = "SHA-256=47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=";
        assertThat(returnedResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldGetDateHeaderValue() {
        // when
        final String returnedResponse = lclHeaderValueProvider.getDateHeaderValue();

        // then
        final String expectedResponse = "Thu, 1 Jan 1970 01:00:00 +0100";
        assertThat(returnedResponse).isEqualTo(expectedResponse);
    }

    private static LclSignatureProvider createLclSignatureProviderMock() {
        final LclSignatureProvider signatureProviderMock = mock(LclSignatureProvider.class);

        when(signatureProviderMock.signRequest(REQUEST_ID, DATE, DIGEST)).thenReturn(SIGNATURE);

        return signatureProviderMock;
    }

    private static Clock createClockMock() {
        final Clock clockMock = mock(Clock.class);

        when(clockMock.instant()).thenReturn(NOW);
        when(clockMock.getZone()).thenReturn(ZONE_ID);

        return clockMock;
    }
}
