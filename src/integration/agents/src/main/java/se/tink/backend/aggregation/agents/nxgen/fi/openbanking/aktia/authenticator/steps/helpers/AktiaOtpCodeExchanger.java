package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.data.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.ErrorResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OtpAuthenticationResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OtpInfoDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.OtpAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.AktiaOtpDataStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.data.ExchangeOtpCodeStatus;

@RequiredArgsConstructor
@Slf4j
public class AktiaOtpCodeExchanger {

    private static final Pattern OTP_CODE_PATTERN = Pattern.compile("\\d{6}");

    private final AktiaApiClient aktiaApiClient;
    private final AktiaOtpDataStorage otpDataStorage;

    public ExchangeOtpCodeStatus exchangeCode(String otpCode) {
        if (!hasOtpCodeValidFormat(otpCode)) {
            return ExchangeOtpCodeStatus.WRONG_OTP_CODE;
        }

        final OtpAuthenticationResponse otpAuthenticationResponse =
                aktiaApiClient.authenticateWithOtp(otpCode);

        final ExchangeOtpCodeStatus codeStatus =
                getOtpCodeStatusFromResponse(otpAuthenticationResponse);
        if (codeStatus == ExchangeOtpCodeStatus.WRONG_OTP_CODE) {
            storeNewOtpInfo(otpAuthenticationResponse);
        }

        return codeStatus;
    }

    private void storeNewOtpInfo(OtpAuthenticationResponse otpAuthenticationResponse) {
        final OtpAuthenticationResponseDto otpAuthenticationResponseDto =
                otpAuthenticationResponse.getOtpAuthenticationResponseDto();
        final OtpInfoDto otpInfoDto = otpAuthenticationResponseDto.getOtpInfo();

        otpDataStorage.storeInfo(otpInfoDto);
    }

    private static ExchangeOtpCodeStatus getOtpCodeStatusFromResponse(
            OtpAuthenticationResponse otpAuthenticationResponse) {
        if (!otpAuthenticationResponse.isSuccessful()) {
            final ErrorResponseDto errorResponseDto =
                    otpAuthenticationResponse.getErrorResponseDto();

            if (errorResponseDto.getErrorCode() == ErrorCode.ACCOUNT_LOCKED) {
                log.error("Account is locked.");
                return ExchangeOtpCodeStatus.ACCOUNT_LOCKED;
            } else {
                log.error(
                        "Other error returned by the serve, code: '{}', message: '{}'",
                        errorResponseDto.getErrorCode(),
                        errorResponseDto.getMessage());
                return ExchangeOtpCodeStatus.OTHER_ERROR;
            }
        } else {
            final OtpAuthenticationResponseDto otpAuthenticationResponseDto =
                    otpAuthenticationResponse.getOtpAuthenticationResponseDto();

            if (otpAuthenticationResponseDto.isOtpAccepted()) {
                return ExchangeOtpCodeStatus.ACCEPTED;
            } else {
                log.error("OTP was not accepted.");
                return ExchangeOtpCodeStatus.WRONG_OTP_CODE;
            }
        }
    }

    /* Validation useful in agent tests in order not to block user account. */
    private static boolean hasOtpCodeValidFormat(String otpCode) {
        final Matcher matcher = OTP_CODE_PATTERN.matcher(otpCode);

        return matcher.matches();
    }
}
