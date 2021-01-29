package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.CredentialsKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbStorage;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.rpc.ConsentResponse;
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

@AllArgsConstructor
public class DnbAuthenticator implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final DnbStorage storage;
    private final DnbApiClient apiClient;
    private final Credentials credentials;

    @Override
    public void autoAuthenticate() {
        if (!isStoredConsentValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private boolean isStoredConsentValid() {
        return storage.containsConsentId()
                && apiClient.fetchConsentDetails(storage.getConsentId()).isValid();
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        if (StringUtils.isEmpty(credentials.getField(CredentialsKeys.PSU_ID))) {
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.AUTHENTICATION_ERROR);
        } else {
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
        }
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        ConsentResponse consentResponse =
                apiClient.createConsent(strongAuthenticationState.getState());

        storage.storeConsentId(consentResponse.getConsentId());

        return ThirdPartyAppAuthenticationPayload.of(new URL(consentResponse.getScaRedirectLink()));
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) {
        Optional<Map<String, String>> supplementalInfo =
                this.supplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        ThirdPartyAppStatus result;
        if (!supplementalInfo.isPresent()) {
            result = ThirdPartyAppStatus.TIMED_OUT;
        } else {
            ConsentDetailsResponse consentDetails =
                    apiClient.fetchConsentDetails(storage.getConsentId());
            if (consentDetails.isValid()) {
                result = ThirdPartyAppStatus.DONE;
                credentials.setSessionExpiryDate(consentDetails.getValidUntil());
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
