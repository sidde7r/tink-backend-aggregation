package se.tink.sa.agent.pt.ob.sibs.rest.interceptor;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.framework.tools.EncryptionCertificateTool;

@Slf4j
public class SibsMessageSignInterceptor implements ClientHttpRequestInterceptor {

    private static final String NEW_LINE = "\n";
    private static final String COLON_SPACE = ": ";

    private static final List<String> SIGNATURE_HEADERS =
            ImmutableList.of(
                    SibsConstants.HeaderKeys.DIGEST,
                    SibsConstants.HeaderKeys.TPP_TRANSACTION_ID,
                    SibsConstants.HeaderKeys.TPP_REQUEST_ID,
                    SibsConstants.HeaderKeys.DATE);

    @Autowired private EncryptionCertificateTool encryptionCertificateTool;

    @Override
    public ClientHttpResponse intercept(
            final HttpRequest request,
            final byte[] body,
            final ClientHttpRequestExecution execution)
            throws IOException {
        formSignatureAndAddAsHeader(request);
        ClientHttpResponse response = execution.execute(request, body);
        return response;
    }

    private void formSignatureAndAddAsHeader(HttpRequest request) {
        List<String> serializedHeaders = new ArrayList<>();
        List<String> headersIncludedInSignature = new ArrayList<>();

        for (String key : SIGNATURE_HEADERS) {
            if (request.getHeaders().get(key) != null) {
                headersIncludedInSignature.add(key);
                if (request.getHeaders().get(key).size() > 1) {
                    throw new IllegalArgumentException(
                            "Unable to provide more than one value in signature");
                }
                serializedHeaders.add(serializedHeader(key, request.getHeaders().get(key).get(0)));
            }
        }

        String headersIncludedInSignatureString =
                StringUtils.join(headersIncludedInSignature, StringUtils.SPACE);
        String serializedHeadersString = StringUtils.join(serializedHeaders, NEW_LINE);
        String signatureBase64Sha = signMessage(serializedHeadersString);
        String signature = formSignature(signatureBase64Sha, headersIncludedInSignatureString);
        request.getHeaders().add(SibsConstants.HeaderKeys.SIGNATURE, signature);
    }

    private String serializedHeader(String name, String value) {
        return name.toLowerCase() + COLON_SPACE + value;
    }

    private String signMessage(String toSignString) {
        return new String(encryptionCertificateTool.toSHA256withRSA(toSignString), Charsets.UTF_8);
    }

    private String formSignature(String signatureBase64Sha, String headers) {
        return String.format(
                SibsConstants.Formats.SIGNATURE_STRING_FORMAT,
                encryptionCertificateTool.getCertificateSerialNumber(),
                SibsConstants.SignatureValues.RSA_SHA256,
                headers,
                signatureBase64Sha);
    }
}
