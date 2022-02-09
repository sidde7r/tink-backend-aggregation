package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.CONSENT_VALIDITY_IN_DAYS;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
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
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@Slf4j
@RequiredArgsConstructor
public class SparebankController implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SparebankAuthenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;
    private final Credentials credentials;

    private String errorMessage;

    @Override
    public void autoAuthenticate() {
        if (authenticator.hasSessionExpired()) {
            authenticator.clearSessionData();
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) {
        Optional<Map<String, String>> maybeSupplementalInformation =
                supplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        return maybeSupplementalInformation
                .map(
                        supplementalInformation -> {
                            if (supplementalInfoContainsRequiredFields(supplementalInformation)) {
                                authenticator.storeSessionData(
                                        supplementalInformation.get(
                                                SparebankConstants.StorageKeys.FIELD_PSU_ID),
                                        supplementalInformation.get(
                                                SparebankConstants.StorageKeys
                                                        .FIELD_TPP_SESSION_ID));
                                authenticator.handleSuccessfulManualAuth();
                                credentials.setSessionExpiryDate(
                                        LocalDate.now().plusDays(CONSENT_VALIDITY_IN_DAYS));
                                return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);

                            } else {
                                errorMessage =
                                        supplementalInformation.get(
                                                SparebankConstants.StorageKeys.FIELD_MESSAGE);
                                return ThirdPartyAppResponseImpl.create(
                                        ThirdPartyAppStatus.CANCELLED);
                            }
                        })
                .orElse(ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.TIMED_OUT));
    }

    private boolean supplementalInfoContainsRequiredFields(
            Map<String, String> supplementalInformation) {
        return supplementalInformation.containsKey(SparebankConstants.StorageKeys.FIELD_PSU_ID)
                && supplementalInformation.containsKey(
                        SparebankConstants.StorageKeys.FIELD_TPP_SESSION_ID);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        final URL authorizeUrl =
                this.authenticator.buildAuthorizeUrl(strongAuthenticationState.getState());

        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.ofNullable(errorMessage).filter(x -> !x.isEmpty()).map(LocalizableKey::new);
    }
}
