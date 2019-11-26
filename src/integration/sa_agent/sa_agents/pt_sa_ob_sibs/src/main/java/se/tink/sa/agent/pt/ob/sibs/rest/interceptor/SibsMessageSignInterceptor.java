package se.tink.sa.agent.pt.ob.sibs.rest.interceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import se.tink.sa.framework.tools.EncryptionCertificateTool;
import se.tink.sa.framework.tools.JsonUtils;

@Slf4j
public class SibsMessageSignInterceptor implements ClientHttpRequestInterceptor {

    public static final String DIGEST = "Digest";
    private static final String NEW_LINE = "\n";
    private static final String COLON_SPACE = ": ";
    public static final String DIGEST_PREFIX = "SHA-256=";
    public static final String SIGNATURE_STRING_FORMAT =
            "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"";
    public static final String RSA_SHA256 = "rsa-sha256";
    public static final String SIGNATURE = "Signature";
    public static final String TPP_CERTIFICATE = "TPP-Certificate";

    @Autowired private EncryptionCertificateTool encryptionCertificateTool;

    @Override
    public ClientHttpResponse intercept(
            final HttpRequest request,
            final byte[] body,
            final ClientHttpRequestExecution execution)
            throws IOException {
        log.debug("Signing message: ");
        String digest = null;
        if (ArrayUtils.isNotEmpty(body)) {
            String s = new String(body);
            digest = getDigest(s);
            request.getHeaders().add(DIGEST, DIGEST_PREFIX + digest);
        }

        String signature = getSignature(digest, request.getHeaders());
        request.getHeaders()
                .put(
                        SIGNATURE,
                        new ArrayList<String>() {
                            {
                                add(signature);
                            }
                        });
        request.getHeaders()
                .put(
                        TPP_CERTIFICATE,
                        new ArrayList<String>() {
                            {
                                add(encryptionCertificateTool.getCertificate());
                            }
                        });
        ClientHttpResponse response = execution.execute(request, body);
        return response;
    }

    public String getSignature(String digest, HttpHeaders headers) {

        List<String> serializedHeaders = new ArrayList<>();
        if (StringUtils.isNotEmpty(digest)) {
            serializedHeaders.add(serializedHeader(DIGEST, DIGEST_PREFIX + digest));
        }

        for (String key : headers.keySet()) {
            serializedHeaders.add(serializedHeader(key, headers.get(key).toString()));
        }

        byte[] signatureSha =
                encryptionCertificateTool.toSHA256withRSA(
                        StringUtils.join(serializedHeaders, NEW_LINE));
        String signatureBase64Sha = Base64.encodeBase64String(signatureSha);

        return formSignature(
                encryptionCertificateTool.getCertificateSerialNumber(),
                signatureBase64Sha,
                StringUtils.join(headers.keySet(), " "));
    }

    private String serializedHeader(String name, String value) {
        return name.toLowerCase() + COLON_SPACE + value;
    }

    private static String formSignature(
            String clientSigningCertificateSerialNumber,
            String signatureBase64Sha,
            String headers) {
        return String.format(
                SIGNATURE_STRING_FORMAT,
                clientSigningCertificateSerialNumber,
                RSA_SHA256,
                headers,
                signatureBase64Sha);
    }

    public String getDigest(String body) {

        byte[] bytes = JsonUtils.writeAsJson(body).getBytes(StandardCharsets.UTF_8);
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot calculate SHA256.", e);
        }
        md.update(bytes, 0, bytes.length);
        byte[] sha = md.digest();
        return org.apache.commons.codec.binary.Base64.encodeBase64String(sha);
    }
}
