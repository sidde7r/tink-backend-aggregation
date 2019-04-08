package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.filter;

import java.util.Objects;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.TokenEntity;
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
                        .get(BunqConstants.StorageKeys.CLIENT_AUTH_TOKEN, TokenEntity.class)
                        .orElse(null);

        MultivaluedMap<String, Object> headers = httpRequest.getHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON);

        if (Objects.equals(HttpMethod.POST, httpRequest.getMethod())) {
            headers.add("Content-Type", MediaType.APPLICATION_JSON);
        }

        headers.add(
                BunqConstants.Headers.LANGUAGE.getKey(), BunqConstants.Headers.LANGUAGE.getValue());
        headers.add(BunqConstants.Headers.REGION.getKey(), BunqConstants.Headers.REGION.getValue());
        headers.add(BunqConstants.Headers.REQUEST_ID.getKey(), UUIDUtils.generateUUID());
        headers.add(
                BunqConstants.Headers.GEOLOCATION.getKey(),
                BunqConstants.Headers.GEOLOCATION.getValue());
        headers.add(
                BunqConstants.Headers.CACHE_CONTROL.getKey(),
                BunqConstants.Headers.CACHE_CONTROL.getValue());
        headers.add(
                BunqConstants.Headers.CLIENT_AUTH.getKey(),
                tokenEntity == null ? null : tokenEntity.getToken());

        return nextFilter(httpRequest);
    }
}
