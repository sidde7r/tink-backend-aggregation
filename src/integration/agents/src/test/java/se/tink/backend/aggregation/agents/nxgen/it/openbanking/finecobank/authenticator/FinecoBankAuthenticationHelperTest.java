package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoStorage;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccessItem;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccountConsent;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentAuthorizationsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.client.FinecoBankApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;

@RunWith(JUnitParamsRunner.class)
public class FinecoBankAuthenticationHelperTest {
    private static final String TEST_DATE = "2020-12-12";
    private static final String TEST_CONSENT_ID = "test_consent_id";

    private final Credentials mockCredentials = mock(Credentials.class);

    private FinecoBankApiClient mockApiClient;
    private FinecoStorage mockStorage;

    private FinecoBankAuthenticationHelper authenticationHelper;

    private Object[] noBalancesAccessItems() {
        return new Object[] {
            new AccessItem(null, null),
            new AccessItem(Collections.emptyList(), null),
            new AccessItem(null, Collections.emptyList()),
            new AccessItem(null, Collections.singletonList(new AccountConsent("111", null))),
        };
    }

    private Object[] balancesAndTransactionsConsentsDoNotMatch() {
        return new Object[] {
            new AccessItem(
                    Collections.singletonList(new AccountConsent("123", null)),
                    Collections.singletonList(new AccountConsent("111", null))),
            new AccessItem(
                    Collections.singletonList(new AccountConsent("343", null)),
                    Collections.singletonList(new AccountConsent("233", null))),
        };
    }

    @Before
    public void setup() {
        mockApiClient = mock(FinecoBankApiClient.class);
        mockStorage = mock(FinecoStorage.class);
        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);
        authenticationHelper =
                new FinecoBankAuthenticationHelper(
                        mockApiClient,
                        mockStorage,
                        mockCredentials,
                        new ConstantLocalDateTimeSource());
    }

    @Test
    @Parameters(method = "balancesAndTransactionsConsentsDoNotMatch")
    public void storeConsentsShouldThrowExceptionIfBalancesAndAccountsConsentsDoNotMatch(
            AccessItem accessItem) {
        // given
        ConsentAuthorizationsResponse consentAuthorizationsResponse =
                new ConsentAuthorizationsResponse();
        consentAuthorizationsResponse.setAccess(accessItem);
        when(mockApiClient.getConsentAuthorizations(TEST_CONSENT_ID))
                .thenReturn(consentAuthorizationsResponse);

        // when
        Throwable thrown = catchThrowable(authenticationHelper::storeConsents);

        // then
        assertBalancesAndTransactionsConsentsException(thrown);
    }

    @Test
    @Parameters(method = "noBalancesAccessItems")
    public void storeConsentsShouldThrowExceptionIfNoBalancesConsents(AccessItem accessItem) {
        // given
        ConsentAuthorizationsResponse consentAuthorizationsResponse =
                new ConsentAuthorizationsResponse();
        consentAuthorizationsResponse.setAccess(accessItem);
        when(mockApiClient.getConsentAuthorizations(TEST_CONSENT_ID))
                .thenReturn(consentAuthorizationsResponse);

        // when
        Throwable thrown = catchThrowable(authenticationHelper::storeConsents);

        // then
        assertBalancesAndTransactionsConsentsException(thrown);
    }

    @Test
    public void storeConsentsShouldThrowExceptionIfNoTransactionsConsents() {
        // given
        ConsentAuthorizationsResponse consentAuthorizationsResponse =
                new ConsentAuthorizationsResponse();
        consentAuthorizationsResponse.setAccess(
                new AccessItem(Collections.singletonList(new AccountConsent("111", null)), null));
        when(mockApiClient.getConsentAuthorizations(TEST_CONSENT_ID))
                .thenReturn(consentAuthorizationsResponse);

        // when
        Throwable thrown = catchThrowable(authenticationHelper::storeConsents);

        // then
        assertBalancesAndTransactionsConsentsException(thrown);
    }

    private void assertBalancesAndTransactionsConsentsException(Throwable thrown) {
        Assertions.assertThat(thrown)
                .isInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Cause: ThirdPartyAppError.AUTHENTICATION_ERROR");
        ThirdPartyAppException tpae = (ThirdPartyAppException) thrown;
        String userMessage = tpae.getUserMessage().get();
        Assertions.assertThat(userMessage)
                .isEqualTo(ErrorMessages.BOTH_BALANCES_AND_TRANSACTIONS_CONSENTS_NEEDED.get());
    }

    @Test
    public void storeConsentsShouldPutConsentsIntoStorage()
            throws ThirdPartyAppException, ParseException {
        // given
        ConsentAuthorizationsResponse consentAuthorizationsResponse =
                new ConsentAuthorizationsResponse();
        List<AccountConsent> balancesConsent =
                Collections.singletonList(new AccountConsent("111", null));
        List<AccountConsent> transactionsConsent =
                Collections.singletonList(new AccountConsent("111", null));
        consentAuthorizationsResponse.setAccess(
                new AccessItem(balancesConsent, transactionsConsent));
        consentAuthorizationsResponse.setValidUntil(TEST_DATE);
        when(mockApiClient.getConsentAuthorizations(TEST_CONSENT_ID))
                .thenReturn(consentAuthorizationsResponse);

        // when
        authenticationHelper.storeConsents();

        // then
        verify(mockStorage).storeBalancesConsents(balancesConsent);
        verify(mockStorage).storeTransactionsConsents(transactionsConsent);
        verify(mockStorage)
                .storeConsentCreationTime(LocalDateTime.of(1992, 4, 10, 0, 0).toString());
        verify(mockCredentials).setSessionExpiryDate(eq(FORMATTER_DAILY.parse(TEST_DATE)));
    }

    @Test
    public void storeConsentsShouldThrowIfNoValidUntilDateAvailable() {
        // given
        ConsentAuthorizationsResponse consentAuthorizationsResponse =
                new ConsentAuthorizationsResponse();
        List<AccountConsent> consent = Collections.singletonList(new AccountConsent("111", null));
        consentAuthorizationsResponse.setAccess(new AccessItem(consent, consent));
        when(mockApiClient.getConsentAuthorizations(TEST_CONSENT_ID))
                .thenReturn(consentAuthorizationsResponse);

        // when
        Throwable thrown = catchThrowable(authenticationHelper::storeConsents);

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Cause: ThirdPartyAppError.AUTHENTICATION_ERROR");
    }
}
