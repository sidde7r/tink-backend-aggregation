package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.core.util.Base64;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.ClientRegistration;
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

    public WellKnownResponse getProviderConfiguration() {
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

        WellKnownResponse providerConfiguration = getProviderConfiguration();
        cachedProviderKeys = httpClient.request(providerConfiguration.getJwksUri())
                .get(JsonWebKeySet.class);

        return cachedProviderKeys;
    }

    public URL getAuthorizationEndpoint() {
        WellKnownResponse providerConfiguration = getProviderConfiguration();
        return providerConfiguration.getAuthorizationEndpoint();
    }

    public RegistrationResponse registerClient() {
        WellKnownResponse wellKnownResponse = getProviderConfiguration();
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
