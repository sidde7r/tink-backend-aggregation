package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.AccessResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.date.DateFormat;
import se.tink.libraries.date.DateFormat.Zone;

@RequiredArgsConstructor
public class RabobankConsentStatusValidator {

    private static final String VALID_CONSENT_STATUS = "valid";

    private final RabobankApiClient rabobankApiClient;
    private final PersistentStorage persistentStorage;
    private final RabobankSignatureHeaderBuilder signatureHeaderBuilder;
    private final RabobankConfiguration rabobankConfiguration;
    private AccessResponse accessResponse;

    public void validateConsentStatus() {
        fetchNewConsentIfEmpty();
        if (!consentIsValid()) {
            cleanStorage();
            throw SessionError.CONSENT_EXPIRED.exception(
                    ErrorMessages.ERROR_CONSENT_MESSAGE + accessResponse);
        }
    }

    private void fetchNewConsentIfEmpty() {
        if (accessResponse == null) {
            accessResponse = updateConsentStatus();
        }
    }

    private void cleanStorage() {
        removeOauthToken();
        removeConsent();
    }

    private AccessResponse updateConsentStatus() {
        final String consentId = persistentStorage.get(StorageKey.CONSENT_ID);

        throwSessionErrorIfConsentIsNull(consentId);

        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = Psd2Headers.getRequestId();
        final String date =
                DateFormat.getFormattedCurrentDate(RabobankConstants.DATE_FORMAT, Zone.GMT);
        final String signatureHeader =
                signatureHeaderBuilder.buildSignatureHeader(digest, uuid, date);
        final URL consentUrl = rabobankConfiguration.getUrls().buildConsentUrl(consentId);

        ConsentDetailsResponse response;
        try {
            response =
                    rabobankApiClient.getConsentStatus(
                            consentUrl, uuid, digest, signatureHeader, date);
        } catch (HttpResponseException e) {
            // Invalid/Revoked Consent response received code 200. Other than that we throw TE
            throw BankServiceError.BANK_SIDE_FAILURE.exception(String.valueOf(e.getResponse()));
        }
        return response.getAccessResponse();
    }

    private void throwSessionErrorIfConsentIsNull(String consentId) {
        if (Strings.isNullOrEmpty(consentId)) {
            removeOauthToken();
            throw SessionError.CONSENT_INVALID.exception("Missing consent id.");
        }
    }

    private boolean consentIsValid() {
        return !accessResponse.getScopes().isEmpty() && consentForEveryAccountIsValid();
    }

    private boolean consentForEveryAccountIsValid() {
        return accessResponse.getScopes().entrySet().stream()
                .flatMap(map -> map.getValue().stream())
                .allMatch(
                        accountConsent -> VALID_CONSENT_STATUS.equals(accountConsent.getStatus()));
    }

    private void removeOauthToken() {
        persistentStorage.remove(StorageKey.OAUTH_TOKEN);
    }

    private void removeConsent() {
        persistentStorage.remove(StorageKey.CONSENT_ID);
    }
}
