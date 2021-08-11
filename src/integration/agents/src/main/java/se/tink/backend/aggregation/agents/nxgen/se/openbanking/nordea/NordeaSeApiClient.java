package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.DecoupledAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.DecoupledAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.DecoupledAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.DecoupledAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaErrorResponse;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class NordeaSeApiClient extends NordeaBaseApiClient {

    public NordeaSeApiClient(
            AgentComponentProvider componentProvider,
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            QsealcSigner qsealcSigner) {
        super(componentProvider, client, persistentStorage, qsealcSigner, false);
    }

    public DecoupledAuthenticationResponse authenticateDecoupled(String ssn) {
        String requestBody =
                SerializationUtils.serializeToString(new DecoupledAuthenticationRequest(ssn));
        return createRequest(Urls.DECOUPLED_AUTHENTICATION, HttpMethod.POST, requestBody)
                .body(requestBody, MediaType.APPLICATION_JSON)
                .post(DecoupledAuthenticationResponse.class);
    }

    public DecoupledAuthenticationResponse getAuthenticationStatusDecoupled(String sessionId) {
        return createRequest(
                        Urls.DECOUPLED_AUTHENTICATION.concatWithSeparator(sessionId),
                        HttpMethod.GET,
                        null)
                .get(DecoupledAuthenticationResponse.class);
    }

    public DecoupledAuthorizationResponse authorizePsuAccountsDecoupled(String code) {
        String requestBody =
                SerializationUtils.serializeToString(
                        DecoupledAuthorizationRequest.builder()
                                .code(code)
                                .scope(getScopes())
                                .build());
        return createRequest(Urls.DECOUPLED_AUTHORIZATION, HttpMethod.POST, requestBody)
                .body(requestBody, MediaType.APPLICATION_JSON)
                .post(DecoupledAuthorizationResponse.class);
    }

    public OAuth2Token getTokenDecoupled(String code) {
        GetTokenForm form =
                GetTokenForm.builder()
                        .setCode(code)
                        .setGrantType(NordeaBaseConstants.FormValues.AUTHORIZATION_CODE)
                        .build();
        return createRequest(Urls.DECOUPLED_TOKEN, HttpMethod.POST, form.getBodyValue())
                .body(form.getBodyValue(), MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public OAuth2Token refreshTokenDecoupled(String refreshToken) {
        String requestBody = RefreshTokenForm.of(refreshToken).getBodyValue();
        try {
            return createRequest(Urls.DECOUPLED_TOKEN, HttpMethod.POST, requestBody)
                    .body(requestBody, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                    .post(GetTokenResponse.class)
                    .toTinkToken();
        } catch (HttpResponseException e) {
            final NordeaErrorResponse error = e.getResponse().getBody(NordeaErrorResponse.class);
            if (error.isRefreshTokenInvalid()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }
}
