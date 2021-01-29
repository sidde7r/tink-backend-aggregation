package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator;

import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Android;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Ios;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableKey;

public class VolksbankAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final VolksbankAuthenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;

    public VolksbankAuthenticationController(
            SupplementalInformationHelper supplementalInformationHelper,
            VolksbankAuthenticator authenticator,
            StrongAuthenticationState strongAuthenticationState) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        throw SessionError.SESSION_EXPIRED.exception();
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) {

        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);

        if (!authenticator.getConsentStatus().isValid()) {
            throw new IllegalStateException("Invalid consent!");
        }

        ConsentResponse detailedConsent =
                authenticator.getDetailedConsent(strongAuthenticationState.getState());

        return openThirdPartyApp(new URL(detailedConsent.getLinks().getScaRedirect()));
    }

    private ThirdPartyAppResponse<String> collectDetailedConsent() {

        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);

        if (!authenticator.getConsentStatus().isValid()) {
            throw new IllegalStateException(VolksbankConstants.ErrorMessages.INVALID_CONSENT);
        }

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    private ThirdPartyAppResponse<String> openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = getAppPayload(authorizeUrl);
        Preconditions.checkNotNull(payload);

        supplementalInformationHelper.openThirdPartyApp(payload);

        return collectDetailedConsent();
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    public ThirdPartyAppAuthenticationPayload getAppPayload() {

        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();
        URL authorizeUrl =
                this.authenticator.buildAuthorizeUrl(strongAuthenticationState.getState());

        Android androidPayload = new Android();
        androidPayload.setIntent(authorizeUrl.get());

        Ios iOsPayload = new Ios();
        iOsPayload.setAppScheme(authorizeUrl.getScheme());
        iOsPayload.setDeepLinkUrl(authorizeUrl.get());

        payload.setAndroid(androidPayload);
        payload.setIos(iOsPayload);
        return payload;
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
