package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.entities;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.MANDATORY_RESPONSE_TYPES;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.PREFERRED_ID_TOKEN_SIGNING_ALGORITHM;

import com.auth0.jwt.impl.PublicClaims;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticatorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.TinkJwt;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.signer.iface.JwtSigner.Algorithm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.rpc.WellKnownResponse;

public class AuthorizeRequest {

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private WellKnownResponse wellKnownConfiguration;
        private SoftwareStatementAssertion softwareStatement;
        private String redirectUrl;
        private ClientInfo clientInfo;
        private String intentId;
        private String state;
        private String nonce;
        private String callbackUri;
        private ImmutableList.Builder<String> scopes =
                ImmutableList.<String>builder().add(UkOpenBankingV31Constants.Scopes.OPEN_ID);

        public Builder withAccountsScope() {
            this.scopes.add(UkOpenBankingV31Constants.Scopes.ACCOUNTS);
            return this;
        }

        public Builder withPaymentsScope() {
            this.scopes.add(UkOpenBankingV31Constants.Scopes.PAYMENTS);
            return this;
        }

        public Builder withSoftwareStatement(SoftwareStatementAssertion softwareStatement) {
            this.softwareStatement = softwareStatement;
            return this;
        }

        public Builder withWellKnownConfiguration(WellKnownResponse wellKnownConfiguration) {
            this.wellKnownConfiguration = wellKnownConfiguration;
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
                    wellKnownConfiguration, "WellKnownConfiguration must be specified.");
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

            String issuer = wellKnownConfiguration.getIssuer();
            String clientId = clientInfo.getClientId();
            String scope = String.join(" ", scopes.build());

            String redirectUri =
                    Optional.ofNullable(callbackUri).filter(s -> !s.isEmpty()).orElse(redirectUrl);

            String responseTypes = String.join(" ", MANDATORY_RESPONSE_TYPES);

            AuthorizeRequestClaims authorizeRequestClaims = createAuthorizeRequestClaims();

            String preferredAlgorithm =
                    wellKnownConfiguration
                            .getPreferredIdTokenSigningAlg(PREFERRED_ID_TOKEN_SIGNING_ALGORITHM)
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Preferred signing algorithm unknown: only RS256 and PS256 are supported"));
            return TinkJwt.create()
                    .withIssuer(clientId)
                    .withClaim(UkOpenBankingV31Constants.Ps256.PayloadClaims.AUDIENCE, issuer)
                    .withClaim(
                            PublicClaims.EXPIRES_AT,
                            Instant.now().plusSeconds(599).getEpochSecond())
                    .withClaim(UkOpenBankingV31Constants.Params.RESPONSE_TYPE, responseTypes)
                    .withClaim(UkOpenBankingV31Constants.Params.CLIENT_ID, clientId)
                    .withClaim(UkOpenBankingV31Constants.Params.REDIRECT_URI, redirectUri)
                    .withClaim(UkOpenBankingV31Constants.Params.SCOPE, scope)
                    .withClaim(UkOpenBankingV31Constants.Params.STATE, state)
                    .withClaim(UkOpenBankingV31Constants.Params.NONCE, nonce)
                    .withClaim(
                            UkOpenBankingAisAuthenticatorConstants.Params.MAX_AGE,
                            UkOpenBankingAisAuthenticatorConstants.MAX_AGE)
                    .withClaim(
                            UkOpenBankingAisAuthenticatorConstants.Params.CLAIMS,
                            authorizeRequestClaims)
                    .signAttached(Algorithm.valueOf(preferredAlgorithm), signer);
        }

        private AuthorizeRequestClaims createAuthorizeRequestClaims() {
            final boolean essential = true;
            final List<String> arcValues =
                    Collections.singletonList(
                            UkOpenBankingAisAuthenticatorConstants.ACR_SECURE_AUTHENTICATION_RTS);

            final OpenBankingIntentIdEntity openBankingIntentIdEntity =
                    new OpenBankingIntentIdEntity(this.intentId, essential);
            final AcrEntity acrEntity = new AcrEntity(essential, arcValues);

            final IdTokenEntity idTokenEntity =
                    IdTokenEntity.builder()
                            .openbankingIntentId(openBankingIntentIdEntity)
                            .acr(acrEntity)
                            .build();
            final UserInfoEntity userinfoEntity =
                    UserInfoEntity.builder().openbankingIntentId(openBankingIntentIdEntity).build();

            return AuthorizeRequestClaims.builder()
                    .idToken(idTokenEntity)
                    .userinfo(userinfoEntity)
                    .build();
        }
    }
}
