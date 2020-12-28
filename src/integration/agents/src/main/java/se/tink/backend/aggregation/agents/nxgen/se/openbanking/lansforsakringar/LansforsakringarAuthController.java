package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.LansforsakringarAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class LansforsakringarAuthController extends OAuth2AuthenticationController {
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final String strongAuthenticationStateSupplementalKey;
    private final LansforsakringarStorageHelper storageHelper;
    private final LansforsakringarAuthenticator authenticator;

    public LansforsakringarAuthController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            LansforsakringarAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            LansforsakringarStorageHelper storageHelper) {
        super(
                persistentStorage,
                supplementalInformationHelper,
                authenticator,
                credentials,
                strongAuthenticationState);

        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationStateSupplementalKey =
                strongAuthenticationState.getSupplementalKey();
        this.storageHelper = storageHelper;
        this.authenticator = authenticator;
    }

    @Override
    public void autoAuthenticate() {
        if (!authenticator.isConsentValid()) {
            storageHelper.clearSessionData();
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    protected Map<String, String> getCallbackData() throws ThirdPartyAppException {
        return supplementalInformationHelper
                .waitForSupplementalInformation(
                        strongAuthenticationStateSupplementalKey,
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES)
                .orElseThrow(ThirdPartyAppError.TIMED_OUT::exception);
    }
}
