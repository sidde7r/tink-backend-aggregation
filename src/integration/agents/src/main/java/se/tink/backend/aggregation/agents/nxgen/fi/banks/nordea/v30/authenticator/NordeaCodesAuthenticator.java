package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.authenticator.rpc.AuthenticateDone;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.authenticator.rpc.AuthenticateStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n.LocalizableKey;

public class NordeaCodesAuthenticator implements ThirdPartyAppAuthenticator<String> {

    private final NordeaFiApiClient apiClient;
    private final SessionStorage sessionStorage;

    private String authReference;

    public NordeaCodesAuthenticator(
            NordeaFiApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public ThirdPartyAppResponse<String> init() {

        try {
            apiClient.initCodesAuthentication();
        } catch (HttpResponseException e) {

            AuthenticateStatus response = e.getResponse().getBody(AuthenticateStatus.class);
            authReference = response.getReference();
            return response;
        }

        // Initialization of Nordea Codes should always generate a http error.
        throw new IllegalStateException("Initialization of Nordea Codes should always generate "
                + "http error and therefore never reach this point.");
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String nextReference) {

        if (nextReference != null) {

            authReference = nextReference;
        }

        try {

            AuthenticateResponse response = apiClient.pollCodesAuthentication(authReference);
            response.storeTokens(sessionStorage);

        } catch (HttpResponseException e) {

            return e.getResponse().getBody(AuthenticateStatus.class);
        }

        // If request does not generate a http error we have successfully authenticated.
        return new AuthenticateDone();
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {

        return NordeaFiConstants.NordeaCodesPayload.build();
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {

        return Optional.ofNullable(NordeaFiConstants
                .AUTHENTICATION_ERROR_MESSAGE
                .getOrDefault(status, null));
    }
}
