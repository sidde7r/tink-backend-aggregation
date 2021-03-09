package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.HeaderValues;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.Headers;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.Uri;

import agents_platform_agents_framework.org.springframework.http.HttpMethod;
import agents_platform_agents_framework.org.springframework.http.MediaType;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.RequestEntity.BodyBuilder;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.entites.NemIdEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.AccessTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.AccessTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.NemIdParamsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan.rpc.CreditApplicationsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan.rpc.LoansResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class AuthenticationApiClient {

    private final AgentPlatformHttpClient client;
    private final RandomValueGenerator randomValueGenerator;
    private final AuthResponseValidator authResponseValidator;
    private final String languageCode;

    public AuthenticationApiClient(
            AgentPlatformHttpClient client,
            RandomValueGenerator randomValueGenerator,
            String languageCode) {
        this.client = client;
        this.randomValueGenerator = randomValueGenerator;
        this.languageCode = languageCode;
        this.authResponseValidator = new AuthResponseValidator();
    }

    public NemIdParamsResponse getNemIdParameters(String deviceId) {
        BodyBuilder requestBuilder =
                getDefaultBodyBuilder(HttpMethod.GET, Uri.NEM_ID_AUTHENTICATE, deviceId);
        return send(requestBuilder.build(), NemIdParamsResponse.class, new NemIdParamsResponse());
    }

    public AccessTokenResponse postNemIdToken(String signature, String challenge, String deviceId) {
        AccessTokenRequest accessTokenRequest =
                new AccessTokenRequest(new NemIdEntity(signature, challenge));

        BodyBuilder requestBuilder =
                getDefaultBodyBuilder(HttpMethod.POST, Uri.NEM_ID_AUTHENTICATE, deviceId)
                        .contentType(MediaType.APPLICATION_JSON);

        return send(
                requestBuilder.body(getSerializedAccessTokenRequest(accessTokenRequest)),
                AccessTokenResponse.class,
                new AccessTokenResponse());
    }

    private String getSerializedAccessTokenRequest(AccessTokenRequest accessTokenRequest) {
        String serializedAccessTokenRequest =
                SerializationUtils.serializeToString(accessTokenRequest);
        if (serializedAccessTokenRequest != null) {
            return StringUtils.replace(serializedAccessTokenRequest, "/", "\\/");
        }
        throw new IllegalArgumentException("Couldn't serialize access token request");
    }

    public AccountsResponse fetchAccounts(String token, String deviceId) {
        BodyBuilder requestBuilder =
                getDefaultBodyBuilder(HttpMethod.GET, Uri.ACCOUNTS_VIEW, deviceId)
                        .header(Headers.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON);
        return send(requestBuilder.build(), AccountsResponse.class, null);
    }

    public LoansResponse fetchLoans(String token, String deviceId) {
        BodyBuilder requestBuilder =
                getDefaultBodyBuilder(HttpMethod.GET, Uri.LOAN, deviceId)
                        .header(Headers.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON);
        return send(requestBuilder.build(), LoansResponse.class, new LoansResponse());
    }

    public CreditApplicationsResponse fetchCreditApplications(String token, String deviceId) {
        BodyBuilder requestBuilder =
                getDefaultBodyBuilder(HttpMethod.GET, Uri.CREDIT_APPLICATIONS, deviceId)
                        .header(Headers.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON);
        return send(
                requestBuilder.build(),
                CreditApplicationsResponse.class,
                new CreditApplicationsResponse());
    }

    private <T, R> T send(
            RequestEntity<R> requestEntity, Class<T> responseClass, T defaultResponse) {
        ResponseEntity<String> responseEntity = client.exchange(requestEntity, String.class);
        authResponseValidator.validate(responseEntity);
        return deserializeResponseOrGetDefault(responseClass, defaultResponse, responseEntity);
    }

    private <T> T deserializeResponseOrGetDefault(
            Class<T> responseClass, T defaultResponse, ResponseEntity<String> responseEntity) {
        return Optional.ofNullable(
                        SerializationUtils.deserializeFromString(
                                responseEntity.getBody(), responseClass))
                .orElseGet(
                        () -> {
                            log.error(
                                    "Failed to deserialize response into: {}",
                                    responseClass.getSimpleName());
                            return defaultResponse;
                        });
    }

    private BodyBuilder getDefaultBodyBuilder(HttpMethod httpMethod, URI uri, String deviceId) {
        BodyBuilder bodyBuilder = RequestEntity.method(httpMethod, uri);
        return addDefaultHeaders(bodyBuilder, deviceId);
    }

    private BodyBuilder addDefaultHeaders(BodyBuilder bodyBuilder, String deviceId) {
        return bodyBuilder
                .header(Headers.DEVICE_MODEL, HeaderValues.DEVICE_MODEL)
                .header(Headers.USER_AGENT, HeaderValues.USER_AGENT_VALUE)
                .header(Headers.REGION, HeaderValues.DK_REGION)
                .header(Headers.OS, HeaderValues.I_OS)
                .header(Headers.DEVICE_MANUFACTURER, HeaderValues.DEVICE_MANUFACTURER)
                .header(Headers.OS_VERSION, HeaderValues.OS_VERSION)
                .header(Headers.LANGUAGE, languageCode)
                .header(Headers.REQUEST_ID, randomValueGenerator.getUUID().toString())
                .header(Headers.DEVICE_ID, deviceId)
                .header(Headers.ACCEPT_LANGUAGE, HeaderValues.DA_LANGUAGE_ACCEPT)
                .header(Headers.ORIGIN, HeaderValues.APP_ORIGIN)
                .header(Headers.APP_VERSION, LunarConstants.APP_VERSION)
                .header(Headers.ACCEPT_ENCODING, HeaderValues.ENCODING)
                .accept(MediaType.ALL);
    }
}
