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
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.LocalizableKey;

public class N26AppAuthenticator implements ThirdPartyAppAuthenticator<String> {

    private final N26ApiClient apiClient;
    private final SessionStorage storage;

    public N26AppAuthenticator(N26ApiClient n26APiClient, SessionStorage storage) {
        this.apiClient = n26APiClient;
        this.storage = storage;
    }

    @Override
    public ThirdPartyAppResponse<String> init() {

        String mfaToken =
                N26Utils.getFromStorage(storage, N26Constants.Storage.MFA_TOKEN, String.class);
        MultiFactorAppResponse multiFactorAppResponse =
                apiClient.initiate2fa(
                        N26Constants.Body.MultiFactor.APP, MultiFactorAppResponse.class);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING, mfaToken);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference)
            throws AuthenticationException, AuthorizationException {

        Either<ErrorResponse, AuthenticationResponse> authenticationResponses =
                apiClient.pollAppStatus();

        if (authenticationResponses.isLeft()) {
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING, reference);
        }
        // TODO: Work on cancellation and timeout for login
        storage.put(N26Constants.Storage.TOKEN_ENTITY, authenticationResponses.get().getToken());
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE, reference);
    }

    // TODO: Deep Links need to be confirmed
    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {

        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();
        ThirdPartyAppAuthenticationPayload.Android androidPayload =
                new ThirdPartyAppAuthenticationPayload.Android();
        androidPayload.setIntent("number26://");
        androidPayload.setPackageName("de.number26.android");

        payload.setAndroid(androidPayload);
        ThirdPartyAppAuthenticationPayload.Ios iOsPayload =
                new ThirdPartyAppAuthenticationPayload.Ios();
        payload.setIos(iOsPayload);
        ThirdPartyAppAuthenticationPayload.Desktop desktop =
                new ThirdPartyAppAuthenticationPayload.Desktop();
        iOsPayload.setAppScheme("number26://");
        iOsPayload.setDeepLinkUrl("number26://");
        payload.setDesktop(desktop);
        return payload;
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.ofNullable(
                N26Constants.Errors.THIRD_PARTY_APP_ERROR.getOrDefault(status, null));
    }
}
