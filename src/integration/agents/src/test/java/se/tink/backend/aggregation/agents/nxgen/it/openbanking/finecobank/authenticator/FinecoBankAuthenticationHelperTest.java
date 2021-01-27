package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccessItem;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccountConsent;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentAuthorizationsResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class FinecoBankAuthenticationHelperTest {
    private static final String TEST_DATE = "2020-12-12";
    private final Credentials credentials = mock(Credentials.class);

    private Object[] noBalancesAccessItems() {
        return new Object[] {
            new AccessItem(null, null),
            new AccessItem(Collections.emptyList(), null),
            new AccessItem(null, Collections.emptyList()),
            new AccessItem(null, Collections.singletonList(new AccountConsent("111", null))),
        };
    }

    @Test
    @Parameters(method = "noBalancesAccessItems")
    public void storeConsentsShouldThrowExceptionIfNoBalancesConsents(AccessItem accessItem) {
        // given
        FinecoBankApiClient apiClient = mock(FinecoBankApiClient.class);
        ConsentAuthorizationsResponse consentAuthorizationsResponse =
                new ConsentAuthorizationsResponse();
        consentAuthorizationsResponse.setAccess(accessItem);
        when(apiClient.getConsentAuthorizations()).thenReturn(consentAuthorizationsResponse);

        PersistentStorage persistentStorage = mock(PersistentStorage.class);

        FinecoBankAuthenticationHelper authenticationHelper =
                new FinecoBankAuthenticationHelper(apiClient, persistentStorage, credentials);

        // when
        Throwable thrown = catchThrowable(authenticationHelper::storeConsents);

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(ThirdPartyAppException.class)
                .hasMessage(ErrorMessages.INVALID_CONSENT_BALANCES);
    }

    @Test
    public void storeConsentsShouldThrowExceptionIfNoTransactionsConsents() {
        // given
        FinecoBankApiClient apiClient = mock(FinecoBankApiClient.class);
        ConsentAuthorizationsResponse consentAuthorizationsResponse =
                new ConsentAuthorizationsResponse();
        consentAuthorizationsResponse.setAccess(
                new AccessItem(Collections.singletonList(new AccountConsent("111", null)), null));
        when(apiClient.getConsentAuthorizations()).thenReturn(consentAuthorizationsResponse);

        PersistentStorage persistentStorage = mock(PersistentStorage.class);

        FinecoBankAuthenticationHelper authenticationHelper =
                new FinecoBankAuthenticationHelper(apiClient, persistentStorage, credentials);

        // when
        Throwable thrown = catchThrowable(authenticationHelper::storeConsents);

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(ThirdPartyAppException.class)
                .hasMessage(ErrorMessages.INVALID_CONSENT_TRANSACTIONS);
    }

    @Test
    public void storeConsentsShouldPutConsentsIntoStorage()
            throws ThirdPartyAppException, ParseException {
        // given
        FinecoBankApiClient apiClient = mock(FinecoBankApiClient.class);
        ConsentAuthorizationsResponse consentAuthorizationsResponse =
                new ConsentAuthorizationsResponse();
        List<AccountConsent> consent = Collections.singletonList(new AccountConsent("111", null));
        consentAuthorizationsResponse.setAccess(new AccessItem(consent, consent));
        consentAuthorizationsResponse.setValidUntil(TEST_DATE);
        when(apiClient.getConsentAuthorizations()).thenReturn(consentAuthorizationsResponse);

        PersistentStorage persistentStorage = mock(PersistentStorage.class);

        FinecoBankAuthenticationHelper authenticationHelper =
                new FinecoBankAuthenticationHelper(apiClient, persistentStorage, credentials);

        // when
        authenticationHelper.storeConsents();

        // then
        verify(persistentStorage, times(1)).put(StorageKeys.BALANCES_CONSENTS, consent);
        verify(persistentStorage, times(1)).put(StorageKeys.TRANSACTIONS_CONSENTS, consent);
        verify(credentials).setSessionExpiryDate(eq(FORMATTER_DAILY.parse(TEST_DATE)));
    }

    @Test
    public void storeConsentsShouldThrowIfNoValidUntilDateAvailable() {
        // given
        FinecoBankApiClient apiClient = mock(FinecoBankApiClient.class);
        ConsentAuthorizationsResponse consentAuthorizationsResponse =
                new ConsentAuthorizationsResponse();
        List<AccountConsent> consent = Collections.singletonList(new AccountConsent("111", null));
        consentAuthorizationsResponse.setAccess(new AccessItem(consent, consent));
        when(apiClient.getConsentAuthorizations()).thenReturn(consentAuthorizationsResponse);

        PersistentStorage persistentStorage = mock(PersistentStorage.class);

        FinecoBankAuthenticationHelper authenticationHelper =
                new FinecoBankAuthenticationHelper(apiClient, persistentStorage, credentials);

        // when
        Throwable thrown = catchThrowable(authenticationHelper::storeConsents);

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Cause: ThirdPartyAppError.AUTHENTICATION_ERROR");
    }
}
