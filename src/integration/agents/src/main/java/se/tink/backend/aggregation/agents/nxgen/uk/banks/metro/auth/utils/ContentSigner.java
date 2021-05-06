package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.security.PrivateKey;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public interface ContentSigner {

    static <T> String signHeaderContentSignature(
            T request, PrivateKey privateKey, URI url, String deviceId) {
        return String.format(
                "data:%s;key-id:%s;scheme:3",
                ContentSigner.signData(
                        new ContentSignature<>(
                                request, url, MetroServiceConstants.TS_CLIENT_VERSION),
                        privateKey),
                deviceId);
    }
    /**
     * It sign the data with are needed to fill up the `Content-Signature`
     *
     * @param data the content which should be signed
     * @param key EC privateKey private key
     * @param <T> body of the Request
     * @return signature of signed data
     */
    static <T> String signData(ContentSignature<T> data, PrivateKey key) {
        return EncodingUtils.encodeAsBase64String(EllipticCurve.signSha256(key, data.build()));
    }

    class ContentSignature<T> {
        private final T body;
        private final String pathWithQueryParams;
        private final String tsClientVersion;

        public ContentSignature(T body, URI url, String tsClientVersion) {
            this.body = body;
            this.pathWithQueryParams = url.getPath() + "?" + url.getQuery();
            this.tsClientVersion = tsClientVersion;
        }

        byte[] build() {
            ObjectMapper objectMapper = new ObjectMapperFactory().getInstance();
            try {
                String data =
                        pathWithQueryParams
                                + "%%"
                                + tsClientVersion
                                + "%%"
                                + objectMapper.writeValueAsString(body);
                return data.getBytes();
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        String.format("Couldn't be able to parse body `%s`", body), e);
            }
        }
    }
}
