package src.integration.lib.src.test.java.se.tink.backend.aggregation.eidassigner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcAlgorithm;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.agent.sdk.utils.signer.signature.Signature;
import se.tink.backend.aggregation.eidassigner.SignatureHeaderGenerator;

public class SignatureHeaderGeneratorTest {
    private static final String SIGNATURE_HEADER =
            "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"%s\",signature=\"%s\"";
    private static final List<String> HEADERS_TO_SIGN =
            Arrays.asList("(request-target)", "Digest", "TPP-Request-ID", "Date");
    private static final String applicationId = "1234";
    private static SignatureHeaderGenerator signatureGenerator;

    @BeforeClass
    public static void init() {
        QsealcSigner signer = mock(QsealcSigner.class);
        when(signer.sign(any(), any())).thenReturn(Signature.create("FAKE_SIGNATURE\n".getBytes()));

        signatureGenerator =
                new SignatureHeaderGenerator(
                        SIGNATURE_HEADER,
                        HEADERS_TO_SIGN,
                        applicationId,
                        signer,
                        QsealcAlgorithm.RSA_SHA256);
    }

    @Test
    public void shouldCalculateSignatureHeaderProperly() {
        // when
        String signature = signatureGenerator.generateSignatureHeader(getRegularHeaders());

        // then
        assertEquals(
                "keyId=\"1234\",algorithm=\"rsa-sha256\",headers=\"digest tpp-request-id\",signature=\"RkFLRV9TSUdOQVRVUkUK\"",
                signature);
    }

    @Test
    public void shouldGetEqualSignatureHeadersIfTheyDifferJustInNonSignificantHeaders() {
        // when
        String signature1 = signatureGenerator.generateSignatureHeader(getRegularHeaders());
        String signature2 =
                signatureGenerator.generateSignatureHeader(
                        getHeadersExtendedWithInsignificantHeaders());

        // then
        assertEquals("Signatures should be equal", signature1, signature2);
    }

    @Test
    public void shouldGetDifferentSignatureHeadersIfTheyDifferInSignificantHeaders() {
        // when
        String signature1 = signatureGenerator.generateSignatureHeader(getRegularHeaders());
        String signature2 =
                signatureGenerator.generateSignatureHeader(
                        getHeadersExtendedWithSignificantHeaders());

        // then
        assertNotEquals("Signatures should not be equal", signature1, signature2);
    }

    private Map<String, Object> getRegularHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("TPP-Request-ID", "123456789");
        headers.put("Digest", "SHA-256=WiKGYz3sUxuDKbmaLEWL/HxmPAkaEnMYAuOehOL6lGY=");

        return headers;
    }

    private Map<String, Object> getHeadersExtendedWithInsignificantHeaders() {
        Map<String, Object> headers = getRegularHeaders();
        headers.put("Foo", "Bar");
        headers.put("Bar", "Foo");

        return headers;
    }

    private Map<String, Object> getHeadersExtendedWithSignificantHeaders() {
        Map<String, Object> headers = getRegularHeaders();
        headers.put("Date", "Tue, 10 Dec 2019 12:38:29 GMT");

        return headers;
    }
}
