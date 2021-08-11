package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator;

import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoStorage;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.client.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public final class FinecoBankAuthenticationHelper {

    private final FinecoBankApiClient apiClient;
    private final FinecoStorage storage;
    private final Credentials credentials;
    private final LocalDateTimeSource localDateTimeSource;

    public URL buildAuthorizeUrl(String state) {
        AccessEntity accessEntity = AccessEntity.builder().allPsd2(AccessType.ALL_ACCOUNTS).build();

        ConsentRequest consentRequest =
                new ConsentRequest(
                        accessEntity,
                        true,
                        localDateTimeSource
                                .now()
                                .toLocalDate()
                                .plus(90, ChronoUnit.DAYS)
                                .toString(),
                        4,
                        false);

        ConsentResponse consentResponse = apiClient.createConsent(consentRequest, state);
        storage.storeConsentId(consentResponse.getConsentId());
        return new URL(consentResponse.getLinks().getScaRedirect());
    }

    public boolean isStoredConsentValid() {
        String consentId = storage.getConsentId();
        return consentId != null && apiClient.getConsentDetails(consentId).isValid();
    }

    public ConsentDetailsResponse getConsentDetails() {
        return apiClient.getConsentDetails(storage.getConsentId());
    }

    public void storeConsentDetails(ConsentDetailsResponse consentDetails) {
        AccessEntity accessEntity = consentDetails.getAccess();

        validateIfConsentAllowsBothBalancesAndTransactionsOrThrow(accessEntity);

        storage.storeBalancesConsents(accessEntity.getBalances());
        storage.storeTransactionsConsents(accessEntity.getTransactions());
        storage.storeConsentCreationTime(localDateTimeSource.now().toString());
        credentials.setSessionExpiryDate(consentDetails.getValidUntil());
    }

    private void validateIfConsentAllowsBothBalancesAndTransactionsOrThrow(
            AccessEntity accessEntity) {
        List<AccountReferenceEntity> balancesConsents = accessEntity.getBalances();
        List<AccountReferenceEntity> transactionsConsents = accessEntity.getTransactions();
        if (CollectionUtils.isEmpty(balancesConsents)
                || CollectionUtils.isEmpty(transactionsConsents)
                || !CollectionUtils.isEqualCollection(balancesConsents, transactionsConsents)) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception(
                    ErrorMessages.BOTH_BALANCES_AND_TRANSACTIONS_CONSENTS_NEEDED);
        }
    }
}
