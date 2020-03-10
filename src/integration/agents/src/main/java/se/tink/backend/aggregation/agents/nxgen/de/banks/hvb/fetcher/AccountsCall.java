package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBConstants.APP_VERSION;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBConstants.OS_VERSION;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBConstants.PLATFORM;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountsCall extends SimpleExternalApiCall<String, AccountsResponse> {

    private final ConfigurationProvider configurationProvider;
    private final HVBStorage storage;

    public AccountsCall(
            TinkHttpClient httpClient,
            ConfigurationProvider configurationProvider,
            HVBStorage storage) {
        super(httpClient);
        this.configurationProvider = configurationProvider;
        this.storage = storage;
    }

    @Override
    protected HttpRequest prepareRequest(String directBankingNumber) {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(
                        configurationProvider.getBaseUrl()
                                + "/adapters/UC_MBX_GL_BE_FACADE_NJ/accounts_fetchAllAccounts"),
                prepareRequestHeaders(storage.getAccessToken()),
                prepareRequestBody(directBankingNumber));
    }

    private MultivaluedMap<String, Object> prepareRequestHeaders(String authorizationToken) {
        MultivaluedMap<String, Object> headers = configurationProvider.getStaticHeaders();
        headers.putSingle(AUTHORIZATION, authorizationToken);
        headers.putSingle(CONTENT_TYPE, APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private String prepareRequestBody(String directBankingNumber) {
        return Form.builder()
                .encodeSpacesWithPercent()
                .put("params", prepareBodyParams(directBankingNumber))
                .build()
                .serialize();
    }

    private String prepareBodyParams(String directBankingNumber) {
        RequestBody payload = new RequestBody(directBankingNumber);
        return serializeToString(Collections.singleton(payload));
    }

    private String serializeToString(Object obj) {
        return Optional.of(obj)
                .map(SerializationUtils::serializeToString)
                .orElseThrow(() -> new IllegalArgumentException("Couldn't serialize object."));
    }

    @Override
    protected ExternalApiCallResult<AccountsResponse> parseResponse(HttpResponse httpResponse) {
        return ExternalApiCallResult.of(
                httpResponse.getBody(AccountsResponse.class), httpResponse.getStatus());
    }

    @Value
    @JsonObject
    static class RequestBody {

        @JsonProperty("reb")
        private String directBankingNumber;

        private String platform = PLATFORM;
        private String osVersion = OS_VERSION;
        private String appVersion = APP_VERSION;
    }
}
