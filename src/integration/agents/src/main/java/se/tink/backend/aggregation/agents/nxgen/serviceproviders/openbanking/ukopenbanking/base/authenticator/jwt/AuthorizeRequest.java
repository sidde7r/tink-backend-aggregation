package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt;

import com.auth0.jwt.impl.PublicClaims;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticatorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt.entities.AuthorizeRequestClaims;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.PS256.PAYLOAD_CLAIMS;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.Params;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.TinkJwt;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner.Algorithm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;

// This is an Authorize request JWT that is used by UK OpenBanking.
public class AuthorizeRequest {

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private WellKnownResponse wellknownConfiguration;
        private SoftwareStatementAssertion softwareStatement;
        private String redirectUrl;
        private ClientInfo clientInfo;
        private String intentId;
        private String state;
        private String nonce;
        private String callbackUri;
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

        public Builder withSoftwareStatement(SoftwareStatementAssertion softwareStatement) {
            this.softwareStatement = softwareStatement;
            return this;
        }

        public Builder withWellknownConfiguration(WellKnownResponse wellknownConfiguration) {
            this.wellknownConfiguration = wellknownConfiguration;
            return this;
        }

        public Builder withRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
            return this;
        }

        public Builder withClientInfo(ClientInfo clientInfo) {
            this.clientInfo = clientInfo;
            return this;
        }

        public Builder withIntentId(String intentId) {
            this.intentId = intentId;
            return this;
        }

        public Builder withState(String state) {
            this.state = state;
            return this;
        }

        public Builder withNonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public Builder withCallbackUri(String callbackUri) {
            this.callbackUri = callbackUri;
            return this;
        }

        public String build(JwtSigner signer) {
            Preconditions.checkNotNull(
                    wellknownConfiguration, "WellknownConfiguration must be specified.");
            Preconditions.checkNotNull(softwareStatement, "SoftwareStatement must be specified.");
            Preconditions.checkNotNull(clientInfo, "ClientInfo must be specified.");

            if (Strings.isNullOrEmpty(intentId)) {
                throw new IllegalStateException("IntentId cannot be null or empty.");
            }

            if (Strings.isNullOrEmpty(state)) {
                throw new IllegalStateException("State cannot be null or empty.");
            }

            if (Strings.isNullOrEmpty(nonce)) {
                throw new IllegalStateException("Nonce cannot be null or empty.");
            }

            String issuer = wellknownConfiguration.getIssuer();
            String clientId = clientInfo.getClientId();
            String scope = scopes.build().stream().collect(Collectors.joining(" "));

            String redirectUri =
                    Optional.ofNullable(callbackUri).filter(s -> !s.isEmpty()).orElse(redirectUrl);

            String responseTypes =
                    OpenIdConstants.MANDATORY_RESPONSE_TYPES.stream()
                            .collect(Collectors.joining(" "));

            AuthorizeRequestClaims authorizeRequestClaims =
                    new AuthorizeRequestClaims(
                            intentId,
                            UkOpenBankingAisAuthenticatorConstants.ACR_SECURE_AUTHENTICATION_RTS);

            String preferredAlgorithm =
                    wellknownConfiguration
                            .getPreferredIdTokenSigningAlg(
                                    OpenIdConstants.PREFERRED_ID_TOKEN_SIGNING_ALGORITHM)
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Preferred signing algorithm unknown: only RS256 and PS256 are supported"));
            return TinkJwt.create()
                    .withIssuer(clientId)
                    .withClaim(PAYLOAD_CLAIMS.AUDIENCE, issuer)
                    .withClaim(
                            PublicClaims.EXPIRES_AT,
                            Instant.now().plusSeconds(599).getEpochSecond())
                    .withClaim(Params.RESPONSE_TYPE, responseTypes)
                    .withClaim(Params.CLIENT_ID, clientId)
                    .withClaim(Params.REDIRECT_URI, redirectUri)
                    .withClaim(Params.SCOPE, scope)
                    .withClaim(Params.STATE, state)
                    .withClaim(Params.NONCE, nonce)
                    .withClaim(
                            UkOpenBankingAisAuthenticatorConstants.Params.MAX_AGE,
                            UkOpenBankingAisAuthenticatorConstants.MAX_AGE)
                    .withClaim(
                            UkOpenBankingAisAuthenticatorConstants.Params.CLAIMS,
                            authorizeRequestClaims)
                    .signAttached(Algorithm.valueOf(preferredAlgorithm), signer);
        }
    }
}
