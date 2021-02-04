package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class UnicreditAuthenticator {

    private static final List<ErrorCodes> INVALID_CONSENT_ERROR_CODES =
            ImmutableList.of(
                    ErrorCodes.CONSENT_INVALID,
                    ErrorCodes.CONSENT_EXPIRED,
                    ErrorCodes.CONSENT_UNKNOWN);

    private final UnicreditStorage unicreditStorage;
    private final UnicreditBaseApiClient apiClient;
    private final Credentials credentials;

    Optional<String> getConsentId() {
        return unicreditStorage.getConsentId();
    }

    void clearConsent() {
        unicreditStorage.removeConsentId();
    }

    Optional<ConsentDetailsResponse> getConsentDetailsWithValidStatus() {
        try {
            ConsentDetailsResponse consentDetails = apiClient.getConsentDetails();
            return consentDetails.isValid() ? Optional.of(consentDetails) : Optional.empty();

        } catch (HttpResponseException hre) {
            if (isInvalidConsentException(hre)) {
                return Optional.empty();
            }
            throw hre;
        }
    }

    void setCredentialsSessionExpiryDate(ConsentDetailsResponse consentDetailsResponse) {
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }

    URL buildAuthorizeUrl(String state) {
        ConsentResponse consentResponse = apiClient.createConsent(state);
        unicreditStorage.saveConsentId(consentResponse.getConsentId());
        return apiClient.getScaRedirectUrlFromConsentResponse(consentResponse);
    }

    private boolean isInvalidConsentException(HttpResponseException httpResponseException) {
        final String responseBody = httpResponseException.getResponse().getBody(String.class);
        return INVALID_CONSENT_ERROR_CODES.stream()
                .map(ErrorCodes::name)
                .anyMatch(responseBody::contains);
    }
}
