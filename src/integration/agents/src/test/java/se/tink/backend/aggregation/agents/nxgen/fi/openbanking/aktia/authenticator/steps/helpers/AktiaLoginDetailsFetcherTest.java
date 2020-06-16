package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createLoginDetailsResponseForLockedAccount;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createLoginDetailsResponseForMustAcceptTerms;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createLoginDetailsResponseForPasswordChange;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createNotSuccessfulLoginDetailsResponse;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createOtpInfoDto;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createSuccessfulLoginDetailsResponseWithOtpRequired;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createSuccessfulLoginDetailsResponseWithoutOtpRequired;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OtpInfoDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.LoginDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.AktiaOtpDataStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.data.GetLoginDetailsStatus;

public class AktiaLoginDetailsFetcherTest {

    private AktiaLoginDetailsFetcher loginDetailsFetcher;

    private AktiaApiClient aktiaApiClientMock;

    private AktiaOtpDataStorage otpDataStorageMock;

    @Before
    public void setUp() {
        aktiaApiClientMock = mock(AktiaApiClient.class);
        otpDataStorageMock = mock(AktiaOtpDataStorage.class);

        loginDetailsFetcher = new AktiaLoginDetailsFetcher(aktiaApiClientMock, otpDataStorageMock);
    }

    @Test
    public void shouldReturnStatusIfLoginWasSuccessful() {
        // given
        final LoginDetailsResponse loginDetailsResponse =
                createSuccessfulLoginDetailsResponseWithoutOtpRequired();
        when(aktiaApiClientMock.getLoginDetails()).thenReturn(loginDetailsResponse);

        // when
        final GetLoginDetailsStatus returnedStatus = loginDetailsFetcher.getLoginDetails();

        // then
        assertThat(returnedStatus).isEqualTo(GetLoginDetailsStatus.LOGGED_IN);
        verify(otpDataStorageMock, never()).storeInfo(any(OtpInfoDto.class));
    }

    @Test
    public void shouldReturnOtpStatusIfOtpIsRequired() {
        // given
        final LoginDetailsResponse loginDetailsResponse =
                createSuccessfulLoginDetailsResponseWithOtpRequired();
        when(aktiaApiClientMock.getLoginDetails()).thenReturn(loginDetailsResponse);

        // when
        final GetLoginDetailsStatus returnedStatus = loginDetailsFetcher.getLoginDetails();

        // then
        assertThat(returnedStatus).isEqualTo(GetLoginDetailsStatus.OTP_REQUIRED);

        final OtpInfoDto expectedOtpInfo = createOtpInfoDto();
        verify(otpDataStorageMock).storeInfo(expectedOtpInfo);
    }

    @Test
    public void shouldReturnStatusIfLoginDetailsResponseHasErrors() {
        // given
        final LoginDetailsResponse loginDetailsResponse = createNotSuccessfulLoginDetailsResponse();
        when(aktiaApiClientMock.getLoginDetails()).thenReturn(loginDetailsResponse);

        // when
        final GetLoginDetailsStatus returnedStatus = loginDetailsFetcher.getLoginDetails();

        // then
        assertThat(returnedStatus).isEqualTo(GetLoginDetailsStatus.ERROR_IN_RESPONSE);
        verify(otpDataStorageMock, never()).storeInfo(any(OtpInfoDto.class));
    }

    @Test
    public void shouldReturnStatusIfPasswordChangeIsRequired() {
        // given
        final LoginDetailsResponse loginDetailsResponse =
                createLoginDetailsResponseForPasswordChange();
        when(aktiaApiClientMock.getLoginDetails()).thenReturn(loginDetailsResponse);

        // when
        final GetLoginDetailsStatus returnedStatus = loginDetailsFetcher.getLoginDetails();

        // then
        assertThat(returnedStatus).isEqualTo(GetLoginDetailsStatus.PASSWORD_CHANGE_REQUIRED);
        verify(otpDataStorageMock, never()).storeInfo(any(OtpInfoDto.class));
    }

    @Test
    public void shouldReturnStatusIfAccountIsLocked() {
        // given
        final LoginDetailsResponse loginDetailsResponse =
                createLoginDetailsResponseForLockedAccount();
        when(aktiaApiClientMock.getLoginDetails()).thenReturn(loginDetailsResponse);

        // when
        final GetLoginDetailsStatus returnedStatus = loginDetailsFetcher.getLoginDetails();

        // then
        assertThat(returnedStatus).isEqualTo(GetLoginDetailsStatus.ACCOUNT_LOCKED);
        verify(otpDataStorageMock, never()).storeInfo(any(OtpInfoDto.class));
    }

    @Test
    public void shouldReturnStatusIfUserMustAcceptTerms() {
        // given
        final LoginDetailsResponse loginDetailsResponse =
                createLoginDetailsResponseForMustAcceptTerms();
        when(aktiaApiClientMock.getLoginDetails()).thenReturn(loginDetailsResponse);

        // when
        final GetLoginDetailsStatus returnedStatus = loginDetailsFetcher.getLoginDetails();

        // then
        assertThat(returnedStatus).isEqualTo(GetLoginDetailsStatus.MUST_ACCEPT_TERMS);
        verify(otpDataStorageMock, never()).storeInfo(any(OtpInfoDto.class));
    }
}
