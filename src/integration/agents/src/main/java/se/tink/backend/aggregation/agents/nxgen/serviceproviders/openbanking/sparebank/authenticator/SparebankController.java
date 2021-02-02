package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
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
import se.tink.libraries.i18n.LocalizableKey;

@RequiredArgsConstructor
public class SparebankController implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final String FIELD_PSU_ID = "psu-id";
    private static final String FIELD_TPP_SESSION_ID = "tpp-session-id";
    private static final String FIELD_MESSAGE = "message";

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SparebankAuthenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;
    private final Credentials credentials;

    private String errorMessage;

    @Override
    public void autoAuthenticate() {
        if (!authenticator.psuAndSessionPresent() || !authenticator.isTppSessionStillValid()) {
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
                this.supplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        if (maybeSupplementalInformation.isPresent()) {
            Map<String, String> supplementalInformation = maybeSupplementalInformation.get();
            if (supplementalInfoContainsRequiredFields(supplementalInformation)) {
                authenticator.storeSessionData(
                        supplementalInformation.get(FIELD_PSU_ID),
                        supplementalInformation.get(FIELD_TPP_SESSION_ID));
                credentials.setSessionExpiryDate(LocalDate.now().plusDays(90));
                return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
            } else {
                errorMessage = supplementalInformation.get(FIELD_MESSAGE);
                return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.CANCELLED);
            }
        } else {
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.TIMED_OUT);
        }
    }

    private boolean supplementalInfoContainsRequiredFields(
            Map<String, String> supplementalInformation) {
        return supplementalInformation.containsKey(FIELD_PSU_ID)
                && supplementalInformation.containsKey(FIELD_TPP_SESSION_ID);
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
