package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@AllArgsConstructor
@Slf4j
public class CbiConsentRediredtAuthorizationStep {

    private final SupplementalInformationController supplementalInformationController;
    private final CbiGlobeAuthApiClient authApiClient;
    private final CbiStorage storage;

    public void authorizeConsent(CbiRedirectAuthorizable redirectAuthorizable) {
        URL scaRedirectUrl = prepareUrl(redirectAuthorizable.getScaRedirectLink());

        Optional<Map<String, String>> supplementalInfo =
                supplementalInformationController.openThirdPartyAppSync(
                        ThirdPartyAppAuthenticationPayload.of(scaRedirectUrl));

        if (!supplementalInfo.isPresent()) {
            throw ThirdPartyAppError.TIMED_OUT.exception("[CBI] No callback data received");
        }

        if (QueryValues.FAILURE.equalsIgnoreCase(supplementalInfo.get().get(QueryKeys.RESULT))) {
            throw ThirdPartyAppError.CANCELLED.exception(
                    "[CBI] Redirect came back with result == failure");
        }

        checkResultingConsent();
    }

    // CBI can provide us with links that do not work if we do not escape spaces.
    private URL prepareUrl(String scaRedirectLink) {
        return new URL(CbiUrlUtils.encodeBlankSpaces(scaRedirectLink));
    }

    private void checkResultingConsent() {
        if (!authApiClient
                .fetchConsentStatus(storage.getConsentId())
                .getConsentStatus()
                .isValid()) {
            throw ThirdPartyAppError.CANCELLED.exception(
                    "[CBI] Consent not valid in check after successful callback");
        }
    }
}
