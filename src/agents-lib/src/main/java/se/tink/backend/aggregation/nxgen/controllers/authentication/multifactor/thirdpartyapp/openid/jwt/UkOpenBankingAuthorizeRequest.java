package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.entities.AuthorizeRequestClaims;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils.OpenIdSignUtils;

// This is an Authorize request JWT that is used by UK OpenBanking.
// Todo: move this to the UK Openbanking package.
public class UkOpenBankingAuthorizeRequest {

    public static class Builder {
        private WellKnownResponse wellknownConfiguration;
        private SoftwareStatement softwareStatement;
        private ClientInfo clientInfo;
        private String intentId;
        private String state;

        public Builder withSoftwareStatement(SoftwareStatement softwareStatement) {
            this.softwareStatement = softwareStatement;
            return this;
        }

        public Builder withWellknownConfiguration(WellKnownResponse wellknownConfiguration) {
            this.wellknownConfiguration = wellknownConfiguration;
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

        public String build() {
            Preconditions.checkNotNull(wellknownConfiguration,
                    "WellknownConfiguration must be specified.");
            Preconditions.checkNotNull(softwareStatement, "SoftwareStatement must be specified.");
            Preconditions.checkNotNull(clientInfo, "ClientInfo must be specified.");

            if (Strings.isNullOrEmpty(intentId)) {
                throw new IllegalStateException("IntentId cannot be null or empty.");
            }

            if (Strings.isNullOrEmpty(state)) {
                throw new IllegalStateException("State cannot be null or empty.");
            }

            String keyId = softwareStatement.getSigningKeyId();
            Algorithm algorithm = OpenIdSignUtils
                    .getSignatureAlgorithm(softwareStatement.getSigningKey());

            String issuer = wellknownConfiguration.getIssuer();
            String clientId = clientInfo.getClientId();
            String redirectUri = softwareStatement.getRedirectUri();
            String scopes = OpenIdConstants.SCOPES.stream().collect(Collectors.joining(" "));
            String responseTypes = OpenIdConstants.MANDATORY_RESPONSE_TYPES.stream()
                    .collect(Collectors.joining(" "));

            AuthorizeRequestClaims authorizeRequestClaims = new AuthorizeRequestClaims(intentId,
                    OpenIdConstants.ACR_SECURE_AUTHENTICATION_RTS);

            return TinkJwtCreator.create()
                    .withKeyId(keyId)
                    .withIssuer(clientId)
                    .withAudience(issuer)
                    .withClaim(OpenIdConstants.Params.RESPONSE_TYPE, responseTypes)
                    .withClaim(OpenIdConstants.Params.CLIENT_ID, clientId)
                    .withClaim(OpenIdConstants.Params.REDIRECT_URI, redirectUri)
                    .withClaim(OpenIdConstants.Params.SCOPE, scopes)
                    .withClaim(OpenIdConstants.Params.STATE, state)
                    .withClaim(OpenIdConstants.Params.MAX_AGE, OpenIdConstants.MAX_AGE)
                    .withClaim(OpenIdConstants.Params.CLAIMS, authorizeRequestClaims)
                    .sign(algorithm);
        }
    }

    public static Builder create() {
        return new Builder();
    }
}
