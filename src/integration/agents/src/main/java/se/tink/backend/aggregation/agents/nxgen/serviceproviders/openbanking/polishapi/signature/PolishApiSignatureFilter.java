package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.signature;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.JWS_SIGNATURE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.X_JWS_SIGNATURE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.JwsHeaders.getJwsHeaders;

import java.util.TreeMap;
import javax.ws.rs.core.MultivaluedMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = Integer.MAX_VALUE)
public class PolishApiSignatureFilter extends Filter {

    private final QsealcSigner signer;
    private final AgentConfiguration<PolishApiConfiguration> configuration;
    private final boolean shouldAttachHeadersAndUri;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        Object jwsSignature;
        if (shouldAttachHeadersAndUri) {
            String body =
                    httpRequest.getBody() == null
                            ? ""
                            : SerializationUtils.serializeToString(httpRequest.getBody());
            jwsSignature =
                    PolishApiJwsSignature.builder()
                            .uri(getUri(httpRequest))
                            .headers(prepareHeaders(httpRequest.getHeaders()))
                            .body(body)
                            .build();
        } else {
            jwsSignature = httpRequest.getBody();
        }

        String jwsHeader =
                PolishApiJwsSignatureProvider.createJwsHeader(signer, configuration, jwsSignature);

        httpRequest.getHeaders().add(X_JWS_SIGNATURE, jwsHeader);
        httpRequest.getHeaders().add(JWS_SIGNATURE, jwsHeader);
        return nextFilter(httpRequest);
    }

    private String getUri(HttpRequest httpRequest) {
        String path = httpRequest.getURI().getPath();
        String query = httpRequest.getURI().getQuery();

        if (StringUtils.isNotBlank(query)) {
            return path + "?" + query;
        }

        return path;
    }

    private TreeMap<String, String> prepareHeaders(MultivaluedMap<String, Object> headers) {
        TreeMap<String, String> jwsHeaders = new TreeMap<>();
        headers.entrySet().stream()
                .filter(key -> getJwsHeaders().contains(key.getKey().toLowerCase()))
                .forEach(
                        key ->
                                jwsHeaders.put(
                                        key.getKey().toLowerCase(),
                                        key.getValue().get(0).toString()));
        return jwsHeaders;
    }
}
