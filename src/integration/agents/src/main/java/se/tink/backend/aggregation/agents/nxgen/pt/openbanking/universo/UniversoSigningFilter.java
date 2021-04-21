package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import static se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo.UniversoConstants.HeaderFormats.SIGNATURE_HEADER;
import static se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo.UniversoConstants.HeaderKeys.DIGEST;
import static se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo.UniversoConstants.HeaderKeys.SIGNATURE;
import static se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo.UniversoConstants.HeaderKeys.TPP_CERTIFICATE;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MultivaluedMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo.UniversoConstants.HeaderFormats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = Integer.MIN_VALUE)
@RequiredArgsConstructor
public class UniversoSigningFilter extends Filter {

    private final UniversoProviderConfiguration configuration;
    private final QsealcSigner signer;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        MultivaluedMap<String, Object> requestHeaders = httpRequest.getHeaders();
        String requestId =
                Optional.ofNullable((UUID) requestHeaders.getFirst(HeaderKeys.X_REQUEST_ID))
                        .orElseGet(UUID::randomUUID)
                        .toString();
        String body = getBody(httpRequest);
        String digest = createDigest(body);

        requestHeaders.add(TPP_CERTIFICATE, getFormattedCert());
        requestHeaders.add(DIGEST, digest);
        requestHeaders.add(UniversoConstants.HeaderKeys.API_KEY, configuration.getApiKey());
        requestHeaders.add(SIGNATURE, generateSignature(digest, requestId));
        return nextFilter(httpRequest);
    }

    private String getBody(HttpRequest httpRequest) {
        return Optional.ofNullable(httpRequest.getBody())
                .map(SerializationUtils::serializeToString)
                .orElse("");
    }

    private String createDigest(String body) {
        return String.format(
                HeaderFormats.SHA_256, Base64.getEncoder().encodeToString(Hash.sha256(body)));
    }

    private String generateSignature(String digest, String requestId) {
        String formattedMessage =
                String.format(HeaderFormats.QSEAL_HEADERS_SIGNATURE, digest, requestId);
        String signature = signer.getSignatureBase64(formattedMessage.getBytes());
        return String.format(SIGNATURE_HEADER, configuration.getKeyId(), signature);
    }

    @SneakyThrows
    private String getFormattedCert() {
        String certificate =
                CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(
                        configuration.getQseal());
        return String.format(HeaderFormats.CERTIFICATE_FORMAT, certificate);
    }
}
