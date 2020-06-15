package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.OTP_CODE;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createOtpAuthenticationResponseForOtherError;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createOtpAuthenticationResponseWhenAccountIsLocked;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createOtpAuthenticationResponseWhenOtpCodeNotAccepted;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createSuccessfulOtpAuthenticationResponse;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OtpInfoDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.OtpAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.AktiaOtpDataStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.data.ExchangeOtpCodeStatus;

public class AktiaOtpCodeExchangerTest {

    private AktiaOtpCodeExchanger otpCodeExchanger;

    private AktiaApiClient aktiaApiClientMock;

    private AktiaOtpDataStorage otpDataStorageMock;

    @Before
    public void setUp() {
        aktiaApiClientMock = mock(AktiaApiClient.class);
        otpDataStorageMock = mock(AktiaOtpDataStorage.class);

        otpCodeExchanger = new AktiaOtpCodeExchanger(aktiaApiClientMock, otpDataStorageMock);
    }

    @Test
    public void shouldReturnStatusForAcceptedOtpCode() {
        // given
        final OtpAuthenticationResponse otpAuthenticationResponse =
                createSuccessfulOtpAuthenticationResponse();
        when(aktiaApiClientMock.authenticateWithOtp(OTP_CODE))
                .thenReturn(otpAuthenticationResponse);

        // when
        final ExchangeOtpCodeStatus returnedStatus = otpCodeExchanger.exchangeCode(OTP_CODE);

        // then
        assertThat(returnedStatus).isEqualTo(ExchangeOtpCodeStatus.ACCEPTED);
        verify(otpDataStorageMock, never()).storeInfo(any(OtpInfoDto.class));
    }

    @Test
    public void shouldReturnStatusForNotAcceptedOtpCode() {
        // given
        final OtpAuthenticationResponse otpAuthenticationResponse =
                createOtpAuthenticationResponseWhenOtpCodeNotAccepted();
        when(aktiaApiClientMock.authenticateWithOtp(OTP_CODE))
                .thenReturn(otpAuthenticationResponse);

        // when
        final ExchangeOtpCodeStatus returnedStatus = otpCodeExchanger.exchangeCode(OTP_CODE);

        // then
        assertThat(returnedStatus).isEqualTo(ExchangeOtpCodeStatus.WRONG_OTP_CODE);

        final OtpInfoDto expectedOtpInfoDto =
                otpAuthenticationResponse.getOtpAuthenticationResponseDto().getOtpInfo();
        verify(otpDataStorageMock).storeInfo(expectedOtpInfoDto);
    }

    @Test
    public void shouldReturnStatusForLockedAccount() {
        // given
        final OtpAuthenticationResponse otpAuthenticationResponse =
                createOtpAuthenticationResponseWhenAccountIsLocked();
        when(aktiaApiClientMock.authenticateWithOtp(OTP_CODE))
                .thenReturn(otpAuthenticationResponse);

        // when
        final ExchangeOtpCodeStatus returnedStatus = otpCodeExchanger.exchangeCode(OTP_CODE);

        // then
        assertThat(returnedStatus).isEqualTo(ExchangeOtpCodeStatus.ACCOUNT_LOCKED);
        verify(otpDataStorageMock, never()).storeInfo(any(OtpInfoDto.class));
    }

    @Test
    public void shouldReturnStatusForOtherErrorInResponse() {
        // given
        final OtpAuthenticationResponse otpAuthenticationResponse =
                createOtpAuthenticationResponseForOtherError();
        when(aktiaApiClientMock.authenticateWithOtp(OTP_CODE))
                .thenReturn(otpAuthenticationResponse);

        // when
        final ExchangeOtpCodeStatus returnedStatus = otpCodeExchanger.exchangeCode(OTP_CODE);

        // then
        assertThat(returnedStatus).isEqualTo(ExchangeOtpCodeStatus.OTHER_ERROR);
        verify(otpDataStorageMock, never()).storeInfo(any(OtpInfoDto.class));
    }

    @Test
    public void shouldReturnStatusForIncorrectOtpCodeFormat() {
        // given
        final String incorrectOtpCode = "12345";

        // when
        final ExchangeOtpCodeStatus returnedStatus =
                otpCodeExchanger.exchangeCode(incorrectOtpCode);

        // then
        assertThat(returnedStatus).isEqualTo(ExchangeOtpCodeStatus.WRONG_OTP_CODE);

        verify(aktiaApiClientMock, never()).authenticateWithOtp(anyString());
        verify(otpDataStorageMock, never()).storeInfo(any(OtpInfoDto.class));
    }
}
