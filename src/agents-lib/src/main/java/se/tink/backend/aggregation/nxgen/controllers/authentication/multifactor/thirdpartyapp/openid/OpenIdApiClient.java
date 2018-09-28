package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.core.util.Base64;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.ClientRegistration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.UkOpenBankingAuthorizeRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.AccountPermissionRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.ClientCredentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.CredentialRequestForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.JsonWebKeySet;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.RegistrationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class OpenIdApiClient {

    private final TinkHttpClient httpClient;
    private final SoftwareStatement softwareStatement;
    private final ProviderConfiguration providerConfiguration;

    // Internal caching. Do not use these fields directly, always use the getters!
    private WellKnownResponse cachedWellKnownResponse;
    private JsonWebKeySet cachedProviderKeys;

    public OpenIdApiClient(TinkHttpClient httpClient, SoftwareStatement softwareStatement,
            ProviderConfiguration providerConfiguration) {
        this.httpClient = httpClient;
        this.softwareStatement = softwareStatement;
        this.providerConfiguration = providerConfiguration;

        // Softw. Transp. key
        httpClient.setSslClientCertificate(
                softwareStatement.getTransportKeyP12(),
                softwareStatement.getTransportKeyPassword());
    }

    public WellKnownResponse getWellKnownConfiguration() {
        if (Objects.nonNull(cachedWellKnownResponse)) {
            return cachedWellKnownResponse;
        }

        cachedWellKnownResponse = httpClient.request(providerConfiguration.getWellKnownURL())
                .get(WellKnownResponse.class);

        return cachedWellKnownResponse;
    }

    public JsonWebKeySet getProviderKeys() {
        if (Objects.nonNull(cachedProviderKeys)) {
            return cachedProviderKeys;
        }

        WellKnownResponse providerConfiguration = getWellKnownConfiguration();
        cachedProviderKeys = httpClient.request(providerConfiguration.getJwksUri())
                .get(JsonWebKeySet.class);

        return cachedProviderKeys;
    }

    public URL getAuthorizationEndpoint() {
        WellKnownResponse providerConfiguration = getWellKnownConfiguration();
        return providerConfiguration.getAuthorizationEndpoint();
    }

    public RegistrationResponse registerClient() {
        WellKnownResponse wellKnownResponse = getWellKnownConfiguration();
        URL registrationEndpoint = wellKnownResponse.getRegistrationEndpoint();

        String postData = ClientRegistration.create()
                .withSoftwareStatement(softwareStatement)
                .withWellknownConfiguration(wellKnownResponse)
                .build();

        return httpClient.request(registrationEndpoint)
                .type("application/jwt")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(RegistrationResponse.class, postData);
    }

    public ClientCredentials requestClientCredentials() {

        ClientInfo clientInfo = providerConfiguration.getClientInfo();

        return httpClient.request(getWellKnownConfiguration().getTokenEndpoint())
                .type("application/x-www-form-urlencoded")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(CredentialRequestForm.create(
                        clientInfo.getClientId(),
                        clientInfo.getClientSecret()))
                .post(ClientCredentials.class);
    }

    public AccountPermissionResponse requestAccountsApi(ClientCredentials clientCredentials) {

        String bearerToken = String.format("Bearer %s", clientCredentials.getAccessToken());
        return httpClient
                .request(providerConfiguration.getAccountRequestsURL())
                .header(OpenIdConstants.Headers.AUTHORIZATION, bearerToken)
                .header(OpenIdConstants.Headers.X_FAPI_FINANCIAL_ID,
                        providerConfiguration.getOrganizationId())
                .header(OpenIdConstants.Headers.X_FAPI_CUSTOMER_LAST_LOGGED_TIME,
                        OpenIdConstants.DevParams.LAST_LOGIN)
                .header(OpenIdConstants.Headers.X_FAPI_CUSTOMER_IP_ADDRESS,
                        OpenIdConstants.DevParams.TINK_IP)
                .header(OpenIdConstants.Headers.X_FAPI_INTERACTION_ID, UUID.randomUUID())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(AccountPermissionRequest.create())
                .post(AccountPermissionResponse.class);
    }

    public URL authorizeConsent(String intentId) {

        String nonce = UUID.randomUUID().toString();
        String state = UUID.randomUUID().toString();

        //TODO: Create util for these?
        String responseType = OpenIdConstants.MANDATORY_RESPONSE_TYPES.stream()
                .collect(Collectors.joining(" "));

        String scope = getWellKnownConfiguration().verifyAndGetScopes(OpenIdConstants.SCOPES)
                .orElseThrow(() -> new IllegalStateException(
                        "Provider does not support the mandatory scopes.")
                );

        /*  'response_type=id_token' only supports 'response_mode=fragment',
         *  setting 'response_mode=query' has no effect the the moment.
         */
        return httpClient.request(cachedWellKnownResponse.getAuthorizationEndpoint())
                .queryParam(OpenIdConstants.Params.RESPONSE_TYPE, responseType)
                .queryParam(OpenIdConstants.Params.CLIENT_ID,
                        providerConfiguration.getClientInfo().getClientId())
                .queryParam(OpenIdConstants.Params.STATE, state)
                .queryParam(OpenIdConstants.Params.SCOPE, scope)
                .queryParam(OpenIdConstants.Params.NONCE, nonce)
                .queryParam(OpenIdConstants.Params.REDIRECT_URI, softwareStatement.getRedirectUri())
                .queryParam(OpenIdConstants.Params.REQUEST, UkOpenBankingAuthorizeRequest.create()
                        .withClientInfo(providerConfiguration.getClientInfo())
                        .withSoftwareStatement(softwareStatement)
                        .withState(state)
                        .withWellknownConfiguration(cachedWellKnownResponse)
                        .withIntentId(intentId)
                        .build())
                .getUrl();
    }

    private static void printJson(Object json) {
        try {
            System.out.println(new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(json));
        } catch (JsonProcessingException je) {
            throw new IllegalStateException(je);
        }
    }

    private static <T> T fromJson(String json, Class<T> type) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void printEncodedJson(String base64Json) {

        final ObjectMapper mapper = new ObjectMapper();
        final String[] parts = base64Json.split("\\.");

        for (String part : parts) {
            try {
                String jsonString = Base64.base64Decode(part);
                Object json = mapper.readValue(jsonString, Object.class);
                System.out
                        .println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));

            } catch (Exception e) {

                System.out.println(String.format("{ %s }", part));
            }
        }

    }
}
