package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.component.detail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.SignatureHeaderGenerator;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class SignatureHeaderGeneratorTest {
    private QsealcSigner signer;
    private String applicationId = "1234";
    private SignatureHeaderGenerator signatureGenerator;

    @Before
    public void init() {
        signer = mock(QsealcSigner.class);
        when(signer.getSignatureBase64(any())).thenReturn("abcdefghijklmnoprstuw");
        signatureGenerator = new SignatureHeaderGenerator(applicationId, signer);
    }

    @Test
    public void shouldCalculateSignatureProperly() {
        // when
        String signature = signatureGenerator.generateSignatureHeader(getRegularHeaders());

        // then
        assertEquals(
                "keyId=\"1234\",algorithm=\"rsa-sha256\",headers=\"digest tpp-request-id\",signature=\"abcdefghijklmnoprstuw\"",
                signature);
    }

    @Test
    public void shouldGetEqualSignaturesIfTheyDifferJustInNonSignificantHeaders() {
        // when
        String signature1 = signatureGenerator.generateSignatureHeader(getRegularHeaders());
        String signature2 =
                signatureGenerator.generateSignatureHeader(
                        getHeadersExtendedWithInsignificantHeaders());

        // then
        assertEquals("Signatures should be equal", signature1, signature2);
    }

    @Test
    public void shouldGetDifferentSignaturesIfTheyDifferInSignificantHeaders() {
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
