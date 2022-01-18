package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankApiConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class SparebankUtils {

    enum HEADERS_TO_SIGN {
        DATE("date"),
        DIGEST("digest"),
        X_REQUEST_ID("x-request-id"),
        PSU_ID("psu-id"),
        PSU_CORPORATE_ID("psu-corporate-id"),
        TPP_REDIRECT_URI("tpp-redirect-uri");

        private String header;

        HEADERS_TO_SIGN(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }
    }

    public static String generateSignatureHeader(
            SparebankApiConfiguration apiConfiguration,
            QsealcSigner signer,
            Map<String, Object> headers) {
        StringBuilder signedWithHeaderKeys = new StringBuilder();
        StringBuilder signedWithHeaderKeyValues = new StringBuilder();

        Arrays.stream(HEADERS_TO_SIGN.values())
                .map(HEADERS_TO_SIGN::getHeader)
                .filter(headers::containsKey)
                .forEach(
                        header -> {
                            signedWithHeaderKeyValues.append(
                                    String.format("%s: %s%n", header, headers.get(header)));
                            signedWithHeaderKeys.append(
                                    (signedWithHeaderKeys.length() == 0) ? header : " " + header);
                        });

        String signature =
                signer.getSignatureBase64(signedWithHeaderKeyValues.toString().trim().getBytes());

        String encodedSignature =
                Base64.getEncoder()
                        .encodeToString(
                                String.format(
                                                "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"%s\",signature=\"%s\"",
                                                prepareSignatureHeaderKeyId(apiConfiguration),
                                                signedWithHeaderKeys.toString(),
                                                signature)
                                        .getBytes(StandardCharsets.UTF_8));

        return String.format("=?utf-8?B?%s?=", encodedSignature);
    }

    private static String prepareSignatureHeaderKeyId(SparebankApiConfiguration apiConfiguration) {
        return String.format(
                "SN=%s,CA=%s",
                apiConfiguration.getCertificateSerialNumberInHex(),
                apiConfiguration.getCertificateIssuerDN());
    }
}
