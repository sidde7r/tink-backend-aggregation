package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.apiclient;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import se.tink.backend.aggregation.eidassigner.FakeQsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;

public class ArkeaSignatureHeaderCreatorTest {

    private final String qsealcUrl =
            "https://cdn.tink.se/eidas/tink-qsealc-without-whitespaces.pem";
    private final QsealcSigner qsealcSigner = new FakeQsealcSigner();
    private final ArkeaSignatureHeaderCreator signatureHeaderCreator =
            new ArkeaSignatureHeaderCreator(qsealcSigner, qsealcUrl);

    @Test
    public void shouldReturnSignatureHeaderValue() {
        // given
        String path = "dummyPath";
        String requestId = "dummyRequestId";
        String expectedSignatureHeaderValue =
                "keyId=\"https://cdn.tink.se/eidas/tink-qsealc-without-whitespaces.pem\","
                        + "algorithm=\"rsa-sha256\","
                        + "headers=\"(request-target) x-request-id\","
                        + "signature=\"RkFLRV9TSUdOQVRVUkUK\"";

        // when
        String signatureHeaderValue =
                signatureHeaderCreator.createSignatureHeaderValue(HttpMethod.GET, path, requestId);

        // then
        assertThat(signatureHeaderValue).isEqualTo(expectedSignatureHeaderValue);
    }
}
