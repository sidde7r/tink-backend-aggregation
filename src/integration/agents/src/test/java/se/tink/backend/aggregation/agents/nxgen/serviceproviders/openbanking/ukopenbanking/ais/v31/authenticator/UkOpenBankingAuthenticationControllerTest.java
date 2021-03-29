package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UkOpenBankingAuthenticationControllerTest {

    private UkOpenBankingAuthenticationController controller;
    private PersistentStorage storage;

    private ConsentStatusValidator mockedValidator;

    private static final String DUMMY_CONSENT_ID = "DUMMY_CONSENT_ID";
    private static final String DUMMY_ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";

    @Before
    public void setUp() throws Exception {
        this.mockedValidator = mock(ConsentStatusValidator.class);

        this.storage = new PersistentStorage();
        this.controller =
                new UkOpenBankingAuthenticationController(
                        storage,
                        mock(SupplementalInformationHelper.class),
                        mock(UkOpenBankingApiClient.class),
                        mock(OpenIdAuthenticator.class),
                        mock(Credentials.class),
                        mock(StrongAuthenticationState.class),
                        "dummyCallbackUri",
                        mock(RandomValueGenerator.class),
                        mock(OpenIdAuthenticationValidator.class),
                        mockedValidator);
    }

    @Test
    public void shouldCompleteConsentStatusValidationSuccessfully() {
        // given
        given(mockedValidator.isInvalidWithRetry(DUMMY_CONSENT_ID, 2)).willReturn(Boolean.FALSE);

        // expected
        assertThatCode(() -> controller.validateConsentStatus()).doesNotThrowAnyException();
    }

    @Test
    public void shouldThrowSessionExceptionWhenConsentStatusInvalid() {
        // given
        storage.put(
                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID,
                DUMMY_CONSENT_ID);
        storage.put(
                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                DUMMY_ACCESS_TOKEN);
        given(mockedValidator.isInvalidWithRetry(DUMMY_CONSENT_ID, 2)).willReturn(Boolean.TRUE);

        // expected
        assertThatExceptionOfType(SessionException.class)
                .isThrownBy(() -> controller.validateConsentStatus())
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
                .isThrownBy(() -> controller.validateConsentStatus())
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
