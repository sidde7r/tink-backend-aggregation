package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator;

import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoStorage;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccessItem;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentAuthorizationsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.PostConsentBodyRequest;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public final class FinecoBankAuthenticationHelper {

    private final FinecoBankApiClient finecoBankApiClient;
    private final FinecoStorage storage;
    private final Credentials credentials;
    private final LocalDateTimeSource localDateTimeSource;

    public URL buildAuthorizeUrl(String state) {
        AccessEntity accessEntity = new AccessEntity(FormValues.ALL_ACCOUNTS);

        PostConsentBodyRequest postConsentBody =
                new PostConsentBodyRequest(
                        accessEntity,
                        FormValues.FALSE,
                        FormValues.FREQUENCY_PER_DAY,
                        FormValues.TRUE,
                        localDateTimeSource
                                .now()
                                .toLocalDate()
                                .plus(FormValues.NUMBER_DAYS, ChronoUnit.DAYS)
                                .toString());

        ConsentResponse consentResponse = finecoBankApiClient.getConsent(postConsentBody, state);
        storage.storeConsentId(consentResponse.getConsentId());
        return new URL(consentResponse.getLinks().getScaRedirect());
    }

    public boolean isStoredConsentValid() {
        String consentId = storage.getConsentId();
        return consentId != null && finecoBankApiClient.getConsentStatus(consentId).isValid();
    }

    public void storeConsents() throws ThirdPartyAppException {
        ConsentAuthorizationsResponse consentAuthorizations =
                finecoBankApiClient.getConsentAuthorizations(storage.getConsentId());
        AccessItem accessItem = consentAuthorizations.getAccess();

        if (CollectionUtils.isEmpty(accessItem.getBalancesConsents())) {
            throw new ThirdPartyAppException(
                    ThirdPartyAppError.AUTHENTICATION_ERROR,
                    ErrorMessages.INVALID_CONSENT_BALANCES);
        }

        if (CollectionUtils.isEmpty(accessItem.getTransactionsConsents())) {
            throw new ThirdPartyAppException(
                    ThirdPartyAppError.AUTHENTICATION_ERROR,
                    ErrorMessages.INVALID_CONSENT_TRANSACTIONS);
        }

        storage.storeBalancesConsents(accessItem.getBalancesConsents());
        storage.storeTransactionsConsents(accessItem.getTransactionsConsents());
        storage.storeConsentCreationTime(localDateTimeSource.now());
        storeSessionExpiryDateInCredentials(consentAuthorizations);
    }

    private void storeSessionExpiryDateInCredentials(
            ConsentAuthorizationsResponse consentAuthorizations) {
        try {
            credentials.setSessionExpiryDate(
                    FORMATTER_DAILY.parse(consentAuthorizations.getValidUntil()));
        } catch (ParseException e) {
            throw new ThirdPartyAppException(ThirdPartyAppError.AUTHENTICATION_ERROR);
        }
    }
}
