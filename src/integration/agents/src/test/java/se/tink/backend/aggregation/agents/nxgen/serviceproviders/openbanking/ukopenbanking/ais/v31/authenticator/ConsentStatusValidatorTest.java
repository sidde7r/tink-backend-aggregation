package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.AccountPermissionsDataResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ConsentStatusValidatorTest {

    private ConsentStatusValidator validator;

    private UkOpenBankingApiClient mockedApiClient;
    private PersistentStorage storage;
    private AccountPermissionsDataResponseEntity mockedConsent;
    private AccountPermissionResponse mockedConsentResponse;
    private static final String DUMMY_CONSENT_ID = "DUMMY_CONSENT_ID";
    private static final String DUMMY_ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";

    @Before
    public void setUp() throws Exception {
        this.mockedConsent = mock(AccountPermissionsDataResponseEntity.class);
        this.mockedConsentResponse = mock(AccountPermissionResponse.class);
        this.mockedApiClient = mock(UkOpenBankingApiClient.class);
        this.storage = new PersistentStorage();

        this.validator = new ConsentStatusValidator(mockedApiClient, storage);
    }

    @Test
    public void shouldValidateConsentSuccessfully() {
        // given
        given(mockedConsentResponse.getData()).willReturn(mockedConsent);
        given(mockedApiClient.fetchConsent(DUMMY_CONSENT_ID)).willReturn(mockedConsentResponse);
        storage.put(
                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID,
                DUMMY_CONSENT_ID);

        // expected
        assertThatCode(() -> validator.validate()).doesNotThrowAnyException();
    }

    @Test
    public void shouldThrowSessionExceptionWhenConsentStatusInvalid() {
        // given
        given(mockedConsent.isNotAuthorised()).willReturn(Boolean.TRUE);
        given(mockedConsentResponse.getData()).willReturn(mockedConsent);
        given(mockedApiClient.fetchConsent(DUMMY_CONSENT_ID)).willReturn(mockedConsentResponse);
        storage.put(
                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID,
                DUMMY_CONSENT_ID);
        storage.put(
                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                DUMMY_ACCESS_TOKEN);

        // expected
        assertThatExceptionOfType(SessionException.class)
                .isThrownBy(() -> validator.validate())
                .withMessage("Invalid consent status. Expiring the session.");
        assertThat(
                        storage.get(
                                UkOpenBankingV31Constants.PersistentStorageKeys
                                        .AIS_ACCOUNT_CONSENT_ID,
                                String.class))
                .isEmpty();
        assertThat(
                        storage.get(
                                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                                String.class))
                .isEmpty();
    }

    @Test
    public void shouldThrowSessionExceptionWhenMarkedWithErrorFlag() {
        // given
        storage.put(
                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID,
                OpenIdAuthenticatorConstants.CONSENT_ERROR_OCCURRED);
        storage.put(
                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                DUMMY_ACCESS_TOKEN);

        // expected
        assertThatExceptionOfType(SessionException.class)
                .isThrownBy(() -> validator.validate())
                .withMessage(
                        "These credentials were marked with CONSENT_ERROR_OCCURRED flag in the past. Expiring the session.");
        assertThat(storage.get(OpenIdAuthenticatorConstants.CONSENT_ERROR_OCCURRED, String.class))
                .isEmpty();
        assertThat(
                        storage.get(
                                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                                String.class))
                .isEmpty();
    }
}
