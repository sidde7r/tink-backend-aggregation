package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.apiclient;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.*;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.Urls.API_BASE_PATH;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;

@RequiredArgsConstructor
public class ArkeaSignatureHeaderCreator {

    private final QsealcSigner qsealcSigner;
    private final String qsealcUrl;

    public String createSignatureHeaderValue(HttpMethod httpMethod, String path, String requestId) {
        return String.format(
                "%s=\"%s\",%s=\"%s\",%s=\"%s\",%s=\"%s\"",
                SignatureKeys.KEY_ID,
                qsealcUrl,
                SignatureKeys.ALGORITHM,
                SignatureValues.RSA_SHA256,
                SignatureKeys.HEADERS,
                createHeadersValue(),
                SignatureKeys.SIGNATURE,
                createSignedAndEncodedSignatureStringValue(httpMethod, path, requestId));
    }

    private String createSignedAndEncodedSignatureStringValue(
            HttpMethod httpMethod, String path, String requestId) {
        return qsealcSigner.getSignatureBase64(
                createSignatureStringValue(httpMethod, path, requestId).getBytes());
    }

    private String createSignatureStringValue(
            HttpMethod httpMethod, String path, String requestId) {
        return String.format(
                "%s: %s\n%s: %s",
                HeaderKeys.REQUEST_TARGET,
                createRequestTargetValue(httpMethod, path),
                HeaderKeys.X_REQUEST_ID,
                requestId);
    }

    private String createHeadersValue() {
        return String.format("%s %s", HeaderKeys.REQUEST_TARGET, HeaderKeys.X_REQUEST_ID);
    }

    private String createRequestTargetValue(HttpMethod httpMethod, String path) {
        final String pathForRequestTarget = path.replace(API_BASE_PATH, "");
        return String.format("%s %s", httpMethod.name().toLowerCase(), pathForRequestTarget);
    }
}
