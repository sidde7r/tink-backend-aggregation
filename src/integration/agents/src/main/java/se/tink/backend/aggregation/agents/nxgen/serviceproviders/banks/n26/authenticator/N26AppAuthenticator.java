package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator;

import io.vavr.control.Either;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Utils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.app.MultiFactorAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp.ExternalAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp.ExternalThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp.ExternalThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class N26AppAuthenticator implements ExternalAppAuthenticator<String> {

    private final N26ApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public N26AppAuthenticator(
            N26ApiClient n26APiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.apiClient = n26APiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public ExternalThirdPartyAppResponse<String> init() {
        String mfaToken =
                N26Utils.getFromStorage(
                        sessionStorage, N26Constants.Storage.MFA_TOKEN, String.class);
        apiClient.initiate2fa(
                N26Constants.Body.MultiFactor.APP, MultiFactorAppResponse.class, mfaToken);

        return ExternalThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING, mfaToken);
    }

    @Override
    public ExternalThirdPartyAppResponse<String> collect(String reference)
            throws AuthenticationException, AuthorizationException {

        Either<ErrorResponse, AuthenticationResponse> authenticationResponses =
                apiClient.pollAppStatus();

        if (authenticationResponses.isLeft()) {
            return ExternalThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING, reference);
        }
        // TODO: Work on cancellation and timeout for login
        sessionStorage.put(
                N26Constants.Storage.TOKEN_ENTITY, authenticationResponses.get().getToken());
        return ExternalThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE, reference);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
