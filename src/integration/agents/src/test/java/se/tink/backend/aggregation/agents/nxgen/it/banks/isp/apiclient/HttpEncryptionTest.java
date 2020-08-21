package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class HttpEncryptionTest {

    // the below is real data recorded from integration-proxy, request without secret data was
    // chosen
    private static final String BODY =
            "{\"payload\":{\"trxots\":\"999990340995720191125145839813\",\"ots\":\"703734\"}}";
    private static final String ENDPOINT = "/ib/content/api/sec/recuperasmartmobilestep2new";
    private static final String METHOD = "POST";
    private static final String ENCRYPTED_BODY =
            "uuJeY1ncEy3cWJ2KORaXSP987P5d8FBzmDOiXADBseapUpZKboEV7MKB53SvK8iersCCnzgGBEyOfgJWVgtmXXFKXMwo0eE6DsoETLzrGSsammLI83NTl7u9atQsnx8m";
    private static final String SIGNATURE = "zgHLisP1bxl6rHiiRMpxwzyc91wLWsXwQcy1NooWwuA=";

    @Test
    public void shouldCalculateCorrectSignature() {
        // when
        String signature = CryptoUtils.calculateRequestSignature(BODY, ENDPOINT, METHOD);
        // then
        assertThat(signature).isEqualTo(SIGNATURE);
    }

    @Test
    public void shouldDecryptBody() {
        // when
        byte[] decryptedBody = CryptoUtils.decryptResponse(ENCRYPTED_BODY);
        // then
        assertThat(decryptedBody).isEqualTo(BODY.getBytes());
    }

    @Test
    public void shouldEncryptAndDecryptBody() {
        // testing encryption is not easy because the initialization vector is randomly generated.
        // Therefore, encryption and decryption is tested for predictable result.
        // Combined with test above of decryption alone, this should provide full coverage.

        // when
        String encryptedBody = CryptoUtils.encryptRequest(BODY);
        byte[] decryptedBody = CryptoUtils.decryptResponse(encryptedBody);
        // then
        assertThat(decryptedBody).isEqualTo(BODY.getBytes());
    }
}
