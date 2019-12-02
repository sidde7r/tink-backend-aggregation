package se.tink.sa.agent.pt.ob.sibs.rest.interceptor;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.framework.tools.EncryptionCertificateTool;
import se.tink.sa.framework.tools.SecretsHandler;

public class SibsStandardHeadersAppender implements ClientHttpRequestInterceptor {

    @Value("${bank.rest.service.header.aggregator}")
    private String aggregator;

    @Autowired private EncryptionCertificateTool encryptionCertificateTool;

    @Autowired private SecretsHandler secretsHandler;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        if (request.getHeaders().get(SibsConstants.HeaderKeys.DATE) == null) {
            String requestTimestamp =
                    new SimpleDateFormat(
                                    SibsConstants.Formats.CONSENT_BODY_DATE_FORMAT, Locale.ENGLISH)
                            .format(new Date());
            request.getHeaders().add(SibsConstants.HeaderKeys.DATE, requestTimestamp);
        }

        if (ArrayUtils.isNotEmpty(body)) {
            String digest = getDigest(body);

            request.getHeaders()
                    .add(
                            SibsConstants.HeaderKeys.DIGEST,
                            SibsConstants.HeaderValues.DIGEST_PREFIX + digest);
        }

        request.getHeaders().add(SibsConstants.HeaderKeys.X_AGGREGATOR, aggregator);
        request.getHeaders().add(SibsConstants.HeaderKeys.TPP_TRANSACTION_ID, getUuidId());
        request.getHeaders().add(SibsConstants.HeaderKeys.TPP_REQUEST_ID, getUuidId());
        request.getHeaders()
                .add(SibsConstants.HeaderKeys.X_IBM_CLIENT_ID, secretsHandler.getClientId());
        request.getHeaders()
                .add(
                        SibsConstants.HeaderKeys.TPP_CERTIFICATE,
                        encryptionCertificateTool.getCertificate());

        return execution.execute(request, body);
    }

    private String getUuidId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String getDigest(byte[] body) {
        return Base64.getEncoder().encodeToString(sha("SHA-256", body));
    }

    private byte[] sha(String algorithm, final byte[]... datas) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            for (byte[] data : datas) {
                md.update(data);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
