package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.filter;

import java.util.Objects;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.TokenEntity;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.uuid.UUIDUtils;

public class BunqRequiredHeadersFilter extends Filter {
    private SessionStorage sessionStorage;

    public BunqRequiredHeadersFilter(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        TokenEntity tokenEntity =
                sessionStorage
                        .get(BunqBaseConstants.StorageKeys.CLIENT_AUTH_TOKEN, TokenEntity.class)
                        .orElse(null);

        MultivaluedMap<String, Object> headers = httpRequest.getHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON);

        if (Objects.equals(HttpMethod.POST, httpRequest.getMethod())) {
            headers.add("Content-Type", MediaType.APPLICATION_JSON);
        }

        headers.add(
                BunqBaseConstants.Headers.LANGUAGE.getKey(),
                BunqBaseConstants.Headers.LANGUAGE.getValue());
        headers.add(
                BunqBaseConstants.Headers.REGION.getKey(),
                BunqBaseConstants.Headers.REGION.getValue());
        headers.add(BunqBaseConstants.Headers.REQUEST_ID.getKey(), UUIDUtils.generateUUID());
        headers.add(
                BunqBaseConstants.Headers.GEOLOCATION.getKey(),
                BunqBaseConstants.Headers.GEOLOCATION.getValue());
        headers.add(
                BunqBaseConstants.Headers.CACHE_CONTROL.getKey(),
                BunqBaseConstants.Headers.CACHE_CONTROL.getValue());
        headers.add(
                BunqBaseConstants.Headers.CLIENT_AUTH.getKey(),
                tokenEntity == null ? null : tokenEntity.getToken());

        return nextFilter(httpRequest);
    }
}
