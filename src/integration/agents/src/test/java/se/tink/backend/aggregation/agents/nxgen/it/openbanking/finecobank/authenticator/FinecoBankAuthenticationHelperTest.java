package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.time.LocalDate;
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
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.client.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;

@RunWith(JUnitParamsRunner.class)
public class FinecoBankAuthenticationHelperTest {
    private static final String TEST_DATE = "2020-12-12";
    private static final String TEST_CONSENT_ID = "test_consent_id";

    private final Credentials mockCredentials = mock(Credentials.class);

    private FinecoBankApiClient mockApiClient;
    private FinecoStorage mockStorage;

    private FinecoBankAuthenticationHelper authenticationHelper;

    private Object[] noAccessItems() {
        return new Object[] {
            AccessEntity.builder().build(),
            AccessEntity.builder().balances(Collections.emptyList()).build(),
            AccessEntity.builder().transactions(Collections.emptyList()).build(),
            AccessEntity.builder()
                    .transactions(
                            Collections.singletonList(new AccountReferenceEntity("111", null)))
                    .build(),
            AccessEntity.builder()
                    .balances(Collections.singletonList(new AccountReferenceEntity("111", null)))
                    .build()
        };
    }

    private Object[] balancesAndTransactionsConsentsDoNotMatch() {
        return new Object[] {
            AccessEntity.builder()
                    .balances(Collections.singletonList(new AccountReferenceEntity("123", null)))
                    .transactions(
                            Collections.singletonList(new AccountReferenceEntity("111", null)))
                    .build(),
            AccessEntity.builder()
                    .balances(Collections.singletonList(new AccountReferenceEntity("343", null)))
                    .transactions(
                            Collections.singletonList(new AccountReferenceEntity("233", null)))
                    .build()
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
            AccessEntity accessEntity) {
        // given
        ConsentDetailsResponse consentDetailsResponse =
                new ConsentDetailsResponse(accessEntity, null, null, null);
        when(mockApiClient.getConsentDetails(TEST_CONSENT_ID)).thenReturn(consentDetailsResponse);

        // when
        Throwable thrown = catchThrowable(authenticationHelper::storeConsents);

        // then
        assertBalancesAndTransactionsConsentsException(thrown);
    }

    @Test
    @Parameters(method = "noAccessItems")
    public void storeConsentsShouldThrowExceptionIfBalancesOfTransactionAccessesEmpty(
            AccessEntity accessEntity) {
        // given
        ConsentDetailsResponse consentDetailsResponse =
                new ConsentDetailsResponse(accessEntity, null, null, null);
        when(mockApiClient.getConsentDetails(TEST_CONSENT_ID)).thenReturn(consentDetailsResponse);

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
        List<AccountReferenceEntity> accesses =
                Collections.singletonList(new AccountReferenceEntity("111", null));
        ConsentDetailsResponse consentDetailsResponse =
                new ConsentDetailsResponse(
                        AccessEntity.builder().transactions(accesses).balances(accesses).build(),
                        null,
                        TEST_CONSENT_ID,
                        TEST_DATE);
        when(mockApiClient.getConsentDetails(TEST_CONSENT_ID)).thenReturn(consentDetailsResponse);

        // when
        authenticationHelper.storeConsents();

        // then
        verify(mockStorage).storeBalancesConsents(accesses);
        verify(mockStorage).storeTransactionsConsents(accesses);
        verify(mockStorage)
                .storeConsentCreationTime(LocalDateTime.of(1992, 4, 10, 0, 0).toString());
        verify(mockCredentials).setSessionExpiryDate(LocalDate.parse(TEST_DATE));
    }
}
