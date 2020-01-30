package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BpceGroupSignatureHeaderGeneratorTest {

    private static final String REQUEST_ID = "101";
    private static final String KEY_ID = "key-id";
    private static final String SIGNATURE = "beef";

    private BpceGroupSignatureHeaderGenerator bpceGroupSignatureHeaderGenerator;

    private BpceGroupRequestSigner requestSignerMock;

    @Before
    public void setUp() {
        final BpceGroupConfiguration configurationMock = mock(BpceGroupConfiguration.class);
        when(configurationMock.getKeyId()).thenReturn(KEY_ID);

        requestSignerMock = mock(BpceGroupRequestSigner.class);

        bpceGroupSignatureHeaderGenerator =
                new BpceGroupSignatureHeaderGenerator(configurationMock, requestSignerMock);
    }

    @Test
    public void shouldBuildSignatureHeader() {
        // given
        final String expectedStringToSign =
                "(request-target): get /accounts\nx-request-id: " + REQUEST_ID;
        when(requestSignerMock.getSignature(expectedStringToSign)).thenReturn(SIGNATURE);

        // when
        final String resultSignatureHeader =
                bpceGroupSignatureHeaderGenerator.buildSignatureHeader(
                        HttpMethod.GET, new URL("https://server/accounts"), REQUEST_ID);

        // then
        final String expectedSignatureHeader =
                "keyId=\"key-id\",algorithm=\"rsa-sha256\",headers=\"(request-target) x-request-id\",signature=\"beef\"";
        assertThat(resultSignatureHeader).isEqualTo(expectedSignatureHeader);
    }

    @Test
    public void shouldBuildSignatureHeaderForUrlWithQueryParams() {
        // given
        final String url = "/accounts/1234/transactions?dateFrom=2020-01-01&dateTo=2020-01-02";
        final String expectedStringToSign =
                "(request-target): get " + url + "\nx-request-id: " + REQUEST_ID;
        when(requestSignerMock.getSignature(expectedStringToSign)).thenReturn(SIGNATURE);

        // when
        final String resultSignatureHeader =
                bpceGroupSignatureHeaderGenerator.buildSignatureHeader(
                        HttpMethod.GET, new URL("https://server" + url), REQUEST_ID);

        // then
        final String expectedSignatureHeader =
                "keyId=\"key-id\",algorithm=\"rsa-sha256\",headers=\"(request-target) x-request-id\",signature=\"beef\"";
        assertThat(resultSignatureHeader).isEqualTo(expectedSignatureHeader);
    }
}
