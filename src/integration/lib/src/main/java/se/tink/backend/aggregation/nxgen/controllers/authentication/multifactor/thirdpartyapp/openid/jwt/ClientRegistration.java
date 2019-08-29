package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import net.minidev.json.JSONObject;
import se.tink.backend.aggregation.agents.utils.crypto.PS256;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.PS256.PAYLOAD_CLAIMS;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils.JwtUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils.OpenIdSignUtils;

public class ClientRegistration {

    public static class Builder {
        private WellKnownResponse wellknownConfiguration;
        private SoftwareStatement softwareStatement;
        private ImmutableList.Builder<String> scopes =
                ImmutableList.<String>builder().add(OpenIdConstants.Scopes.OPEN_ID);

        public Builder withAccountsScope() {
            this.scopes.add(OpenIdConstants.Scopes.ACCOUNTS);
            return this;
        }

        public Builder withPaymentsScope() {
            this.scopes.add(OpenIdConstants.Scopes.PAYMENTS);
            return this;
        }

        public Builder withSoftwareStatement(SoftwareStatement softwareStatement) {
            this.softwareStatement = softwareStatement;
            return this;
        }

        public Builder withWellknownConfiguration(WellKnownResponse wellknownConfiguration) {
            this.wellknownConfiguration = wellknownConfiguration;
            return this;
        }

        public String build() {
            Preconditions.checkNotNull(
                    wellknownConfiguration, "WellknownConfiguration must be specified.");
            Preconditions.checkNotNull(softwareStatement, "SoftwareStatement must be specified.");

            // This should not happen. We rely on these grant types to use the OpenId hybrid flow.
            if (!wellknownConfiguration.hasGrantTypes(OpenIdConstants.MANDATORY_GRANT_TYPES)) {
                throw new IllegalStateException(
                        "Provider does not support the mandatory grant types.");
            }

            // This should not happen. We rely on these response types to use the OpenId hybrid
            // flow.
            if (!wellknownConfiguration.hasResponseTypes(
                    OpenIdConstants.MANDATORY_RESPONSE_TYPES)) {
                throw new IllegalStateException(
                        "Provider does not support the mandatory response types.");
            }

            // Use all grant types they support (this will cover refresh_token if they support it!)
            List<String> grantTypes = wellknownConfiguration.getGrantTypesSupported();

            // Claim to use all types they support. We will only specify our mandatory when we ask
            // for data.
            List<String> responseTypes = wellknownConfiguration.getResponseTypesSupported();

            String idTokenSigningAlg =
                    wellknownConfiguration
                            .getPreferredIdTokenSigningAlg(
                                    OpenIdConstants.PREFERRED_ID_TOKEN_SIGNING_ALGORITHM)
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Preferred id_token sign alg. not found."));

            String tokenEndpointSigningAlg =
                    wellknownConfiguration
                            .getPreferredTokenEndpointSigningAlg(
                                    OpenIdConstants.PREFERRED_TOKEN_ENDPOINT_SIGNING_ALGORITHM)
                            .orElse("");

            String requestObjectSigningAlg =
                    wellknownConfiguration
                            .getPreferredRequestObjectSigningAlg(
                                    OpenIdConstants.PREFERRED_REQUEST_OBJECT_SIGNING_ALGORITHM)
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Preferred request object sign alg. not found."));

            String tokenEndpointAuthMethod =
                    wellknownConfiguration
                            .getPreferredTokenEndpointAuthMethod(
                                    OpenIdConstants.PREFERRED_TOKEN_ENDPOINT_AUTH_METHODS)
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Preferred token endpoint auth method not found."))
                            .toString();

            String issuer = wellknownConfiguration.getIssuer();

            String scope =
                    wellknownConfiguration
                            .verifyAndGetScopes(scopes.build())
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Provider does not support the mandatory scopes."));

            String keyId = softwareStatement.getSigningKeyId();
            Algorithm algorithm =
                    OpenIdSignUtils.getSignatureAlgorithm(softwareStatement.getSigningKey());

            String softwareId = softwareStatement.getSoftwareId();

            String softwareStatementAssertion = softwareStatement.getAssertion();

            // Issued = Now, Expires = Now + 1h
            Date issuedAt = new Date();
            Date expiresAt = JwtUtils.addHours(issuedAt, 1);

            String jwtId = JwtUtils.generateId();
            String preferredAlgorithm =
                    wellknownConfiguration
                            .getPreferredIdTokenSigningAlg(
                                    OpenIdConstants.PREFERRED_ID_TOKEN_SIGNING_ALGORITHM)
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Preferred signing algorithm unknown: only RS256 and PS256 are supported"));

            switch (OpenIdConstants.SIGNING_ALGORITHM.valueOf(preferredAlgorithm)) {
                case PS256:
                    return signWithPs256(
                            grantTypes,
                            responseTypes,
                            idTokenSigningAlg,
                            tokenEndpointSigningAlg,
                            requestObjectSigningAlg,
                            tokenEndpointAuthMethod,
                            issuer,
                            scope,
                            keyId,
                            softwareId,
                            softwareStatementAssertion,
                            issuedAt,
                            expiresAt,
                            jwtId);
                case RS256:
                default:
                    return signWithRs256(
                            grantTypes,
                            responseTypes,
                            idTokenSigningAlg,
                            tokenEndpointSigningAlg,
                            requestObjectSigningAlg,
                            tokenEndpointAuthMethod,
                            issuer,
                            scope,
                            keyId,
                            algorithm,
                            softwareId,
                            softwareStatementAssertion,
                            issuedAt,
                            expiresAt,
                            jwtId);
            }
        }

        private String signWithRs256(
                List<String> grantTypes,
                List<String> responseTypes,
                String idTokenSigningAlg,
                String tokenEndpointSigningAlg,
                String requestObjectSigningAlg,
                String tokenEndpointAuthMethod,
                String issuer,
                String scope,
                String keyId,
                Algorithm algorithm,
                String softwareId,
                String softwareStatementAssertion,
                Date issuedAt,
                Date expiresAt,
                String jwtId) {
            return JWT.create()
                    .withKeyId(keyId)
                    .withJWTId(jwtId)
                    .withIssuedAt(issuedAt)
                    .withExpiresAt(expiresAt)
                    .withIssuer(softwareId)
                    .withAudience(issuer)
                    .withClaim(OpenIdConstants.Params.SOFTWARE_ID, softwareId)
                    .withClaim(
                            OpenIdConstants.Params.SOFTWARE_STATEMENT, softwareStatementAssertion)
                    .withClaim(OpenIdConstants.Params.SCOPE, scope)
                    .withClaim(
                            OpenIdConstants.Params.TOKEN_ENDPOINT_AUTH_METHOD,
                            tokenEndpointAuthMethod)
                    .withClaim(
                            OpenIdConstants.Params.ID_TOKEN_SIGNED_RESPONSE_ALG, idTokenSigningAlg)
                    .withClaim(
                            OpenIdConstants.Params.TOKEN_ENDPOINT_AUTH_SIGNING_ALG,
                            tokenEndpointSigningAlg)
                    .withClaim(
                            OpenIdConstants.Params.REQUEST_OBJECT_SIGNING_ALG,
                            requestObjectSigningAlg)
                    .withClaim(
                            OpenIdConstants.Params.APPLICATION_TYPE,
                            OpenIdConstants.ParamDefaults.WEB)
                    .withArrayClaim(
                            OpenIdConstants.Params.REDIRECT_URIS,
                            softwareStatement.getAllRedirectUris())
                    .withArrayClaim(
                            OpenIdConstants.Params.GRANT_TYPES,
                            JwtUtils.listToStringArray(grantTypes))
                    .withArrayClaim(
                            OpenIdConstants.Params.RESPONSE_TYPES,
                            JwtUtils.listToStringArray(responseTypes))
                    .sign(algorithm);
        }

        private String signWithPs256(
                List<String> grantTypes,
                List<String> responseTypes,
                String idTokenSigningAlg,
                String tokenEndpointSigningAlg,
                String requestObjectSigningAlg,
                String tokenEndpointAuthMethod,
                String issuer,
                String scope,
                String keyId,
                String softwareId,
                String softwareStatementAssertion,
                Date issuedAt,
                Date expiresAt,
                String jwtId) {

            JSONObject object =
                    createPayload(
                            grantTypes,
                            responseTypes,
                            idTokenSigningAlg,
                            tokenEndpointSigningAlg,
                            requestObjectSigningAlg,
                            tokenEndpointAuthMethod,
                            issuer,
                            scope,
                            softwareId,
                            softwareStatementAssertion,
                            issuedAt,
                            expiresAt,
                            jwtId);

            JWSHeader header =
                    new JWSHeader(
                            JWSAlgorithm.PS256,
                            JOSEObjectType.JWT,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            keyId,
                            null,
                            null);
            return PS256.sign(header, object, softwareStatement.getSigningKey());
        }

        private JSONObject createPayload(
                List<String> grantTypes,
                List<String> responseTypes,
                String idTokenSigningAlg,
                String tokenEndpointSigningAlg,
                String requestObjectSigningAlg,
                String tokenEndpointAuthMethod,
                String issuer,
                String scope,
                String softwareId,
                String softwareStatementAssertion,
                Date issuedAt,
                Date expiresAt,
                String jwtId) {
            JSONObject object = new JSONObject();
            object.put(OpenIdConstants.Params.SOFTWARE_ID, softwareId);
            object.put(OpenIdConstants.Params.SOFTWARE_STATEMENT, softwareStatementAssertion);
            object.put(OpenIdConstants.Params.SCOPE, scope.split(" "));
            object.put(OpenIdConstants.Params.TOKEN_ENDPOINT_AUTH_METHOD, tokenEndpointAuthMethod);
            object.put(OpenIdConstants.Params.ID_TOKEN_SIGNED_RESPONSE_ALG, idTokenSigningAlg);
            object.put(
                    OpenIdConstants.Params.TOKEN_ENDPOINT_AUTH_SIGNING_ALG,
                    tokenEndpointSigningAlg);
            object.put(OpenIdConstants.Params.REQUEST_OBJECT_SIGNING_ALG, requestObjectSigningAlg);
            object.put(OpenIdConstants.Params.APPLICATION_TYPE, OpenIdConstants.ParamDefaults.WEB);
            object.put(
                    OpenIdConstants.Params.REDIRECT_URIS, softwareStatement.getAllRedirectUris());
            object.put(OpenIdConstants.Params.GRANT_TYPES, JwtUtils.listToStringArray(grantTypes));
            object.put(
                    OpenIdConstants.Params.RESPONSE_TYPES,
                    JwtUtils.listToStringArray(responseTypes));

            object.put(PAYLOAD_CLAIMS.ISSUER, OpenIdConstants.TINK_UKOPENBANKING_ORGID);
            object.put(PAYLOAD_CLAIMS.ISSUED_AT, issuedAt);
            object.put(PAYLOAD_CLAIMS.EXPIRES_AT, expiresAt);
            object.put(PAYLOAD_CLAIMS.AUDIENCE, issuer);
            object.put(PAYLOAD_CLAIMS.JWT_ID, UUID.randomUUID().toString());
            object.put(PAYLOAD_CLAIMS.TLS_CLIENT_AUTH_DN, OpenIdConstants.TINK_TLS_AUTH_CLIENT_DN);
            return object;
        }
    }

    public static Builder create() {
        return new Builder();
    }
}
