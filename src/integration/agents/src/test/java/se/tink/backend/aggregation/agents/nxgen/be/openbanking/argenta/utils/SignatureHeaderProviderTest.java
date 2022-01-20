package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.HeadersToSign;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcAlgorithm;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.agent.sdk.utils.signer.signature.Signature;

public class SignatureHeaderProviderTest {

    private SignatureHeaderProvider signatureHeaderProvider;

    @Before
    public void init() {
        CertificateValues certificateValues = mock(CertificateValues.class);
        when(certificateValues.getSerialNumber()).thenReturn("SERIAL_NUM");
        when(certificateValues.getCertificateAuthority()).thenReturn("DUMMY_CA");

        QsealcSigner qsealcSigner = mock(QsealcSigner.class);
        String headersWithValues =
                "digest: HEADER_TO_SIGN_ONE\n" + "x-request-id: HEADER_TO_SIGN_TWO";
        when(qsealcSigner.sign(QsealcAlgorithm.RSA_SHA256, headersWithValues.getBytes()))
                .thenReturn(Signature.create("FAKE_SIGNATURE\n".getBytes()));
        signatureHeaderProvider = new SignatureHeaderProvider(qsealcSigner, certificateValues);
    }

    @Test
    public void shouldGenerateSignatureHeader() {
        // given
        Map<String, Object> headers = new HashMap<>();
        headers.put("HEADER_NOT_TO_SIGN", "VALUE");
        headers.put(HeadersToSign.DIGEST.getHeader(), "HEADER_TO_SIGN_ONE");
        headers.put(HeadersToSign.X_REQUEST_ID.getHeader(), "HEADER_TO_SIGN_TWO");

        // when
        String result = signatureHeaderProvider.generateSignatureHeader(headers);

        // then
        assertThat(result)
                .isEqualTo(
                        "keyId=\"SN=SERIAL_NUM,CA=DUMMY_CA\",algorithm=\"rsa-sha256\",headers=\"digest x-request-id\",signature=\"RkFLRV9TSUdOQVRVUkUK\"");
    }
}
