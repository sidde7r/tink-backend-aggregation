package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator;

import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.LocalizableKey;

public class BecController implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final PersistentStorage persistentStorage;
    private final BecAuthenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;

    public BecController(
            final SupplementalInformationHelper supplementalInformationHelper,
            PersistentStorage persistentStorage,
            final BecAuthenticator authenticator,
            StrongAuthenticationState strongAuthenticationState) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.persistentStorage = persistentStorage;
        this.authenticator = authenticator;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        if (Strings.isNullOrEmpty(persistentStorage.get(StorageKeys.CONSENT_ID))
                || !authenticator.isStoredConsentValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) {
        Optional<Map<String, String>> supplementalInfo =
                supplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        ThirdPartyAppStatus result;
        if (!supplementalInfo.isPresent()) {
            result = ThirdPartyAppStatus.TIMED_OUT;
        } else if (authenticator.isStoredConsentValid()) {
            result = ThirdPartyAppStatus.DONE;
        } else {
            result = ThirdPartyAppStatus.AUTHENTICATION_ERROR;
        }
        return ThirdPartyAppResponseImpl.create(result);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        final URL authorizeUrl = authenticator.authenticate(strongAuthenticationState.getState());
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
