package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RequiredArgsConstructor
public final class FinecoBankAuthenticator
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private final FinecoBankAuthenticationHelper authenticationHelper;
    private final SupplementalInformationController supplementalInformationController;
    private final StrongAuthenticationState strongAuthenticationState;

    @Override
    public void autoAuthenticate() {
        if (!authenticationHelper.isStoredConsentValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        return ThirdPartyAppAuthenticationPayload.of(
                authenticationHelper.buildAuthorizeUrl(strongAuthenticationState.getState()));
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) {
        Optional<Map<String, String>> supplementalInfo =
                supplementalInformationController.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        ThirdPartyAppStatus result;
        if (!supplementalInfo.isPresent()) {
            result = ThirdPartyAppStatus.TIMED_OUT;
        } else {
            ConsentDetailsResponse consentDetails = authenticationHelper.getConsentDetails();
            if (consentDetails.isValid()) {
                authenticationHelper.storeConsentDetails(consentDetails);
                result = ThirdPartyAppStatus.DONE;
            } else {
                result = ThirdPartyAppStatus.AUTHENTICATION_ERROR;
            }
        }

        return ThirdPartyAppResponseImpl.create(result);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
