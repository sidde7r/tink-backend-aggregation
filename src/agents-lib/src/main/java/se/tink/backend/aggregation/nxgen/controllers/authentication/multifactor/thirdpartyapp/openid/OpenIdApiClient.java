package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.core.util.Base64;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.OpenIdKeyConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.JsonWebKeySet;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.RegistrationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils.OpenIdSignUtils;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class OpenIdApiClient {
    private static final SecureRandom random = new SecureRandom();


    private final TinkHttpClient httpClient;
//    private final URL wellKnownEndpoint;
    private final SoftwareStatement softwareStatement;
    private final ProviderConfiguration providerConfiguration;

    // Internal caching. Do not use these fields directly, always use the getters!
    private WellKnownResponse cachedWellKnownResponse;
    private JsonWebKeySet cachedProviderKeys;

    public OpenIdApiClient(TinkHttpClient httpClient, SoftwareStatement softwareStatement, ProviderConfiguration providerConfiguration) {
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

    private Date addHours(Date input, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(input);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }

    private String generateId() {
        byte[] id = new byte[16];
        random.nextBytes(id);
        return EncodingUtils.encodeAsBase64String(id);
    }

    public RegistrationResponse registerClient() {

        WellKnownResponse wellKnownResponse = getProviderConfiguration();
        URL registrationEndpoint = wellKnownResponse.getRegistrationEndpoint();

        String jwtId = generateId();
        Date issuedAt = new Date();
        Date expiresAt = addHours(issuedAt, 1);

        //TODO: Move to config and make dynamic
        String idTokenSigningAlg = wellKnownResponse.getPreferredIdTokenSigningAlg(new String[]{"RS256"}).get();
        String tokenEndpointSigningAlg = wellKnownResponse.getPreferredTokenEndpointSigningAlg(new String[]{"RS256"}).get();
        String requestObjectSigningAlg = wellKnownResponse.getPreferredRequestObjectSigningAlg(new String[]{"RS256"}).get();
        Algorithm algorithm = OpenIdSignUtils.getSignatureAlgorithm(softwareStatement.getSigningKey());

        String postData = JWT.create()
                .withKeyId(softwareStatement.getSigningKeyId())
                .withJWTId(jwtId)
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withIssuer(softwareStatement.getSoftwareId())
                .withAudience(wellKnownResponse.getIssuer())
                .withClaim(OpenIdConstants.ClaimParams.SOFTWARE_ID, softwareStatement.getSoftwareId())
                .withClaim(OpenIdConstants.ClaimParams.SOFTWARE_STATEMENT, softwareStatement.getAssertion())
                .withClaim(OpenIdConstants.ClaimParams.SCOPE, wellKnownResponse.verifyAndGetScopes(OpenIdConstants.SCOPES).get())
                .withClaim(OpenIdConstants.ClaimParams.TOKEN_ENDPOINT_AUTH_METHOD, "private_key_jwt")
                .withClaim(OpenIdConstants.ClaimParams.ID_TOKEN_SIGNED_RESPONSE_ALG, idTokenSigningAlg)
                .withClaim(OpenIdConstants.ClaimParams.TOKEN_ENDPOINT_AUTH_SIGNING_ALG, tokenEndpointSigningAlg)
                .withClaim(OpenIdConstants.ClaimParams.REQUEST_OBJECT_SIGNING_ALG, requestObjectSigningAlg)
                .withClaim(OpenIdConstants.ClaimParams.APPLICATION_TYPE, OpenIdConstants.ClaimDefaults.WEB)
                .withArrayClaim(OpenIdConstants.ClaimParams.REDIRECT_URIS, softwareStatement.getRedirectUris())
                .withArrayClaim(OpenIdConstants.ClaimParams.GRANT_TYPES, OpenIdConstants.GRANT_TYPES)
                .withArrayClaim(OpenIdConstants.ClaimParams.RESPONSE_TYPES, OpenIdConstants.RESPONSE_TYPES)
                .sign(algorithm);

        printEncodedJson(postData);
        return httpClient.request(registrationEndpoint)
                .type("application/jwt")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(postData)
                .post(RegistrationResponse.class);
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
