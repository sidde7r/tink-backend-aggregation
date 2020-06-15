package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers;

import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.LoginDetailsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.LoginDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.AktiaOtpDataStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.data.GetLoginDetailsStatus;

@RequiredArgsConstructor
@Slf4j
public class AktiaLoginDetailsFetcher {

    final AktiaApiClient aktiaApiClient;

    @Getter final AktiaOtpDataStorage otpDataStorage;

    public GetLoginDetailsStatus getLoginDetails() {
        final LoginDetailsResponse loginDetailsResponse = aktiaApiClient.getLoginDetails();

        final GetLoginDetailsStatus loginDetailsStatus =
                validateLoginDetailsResponse(loginDetailsResponse);
        if (Objects.nonNull(loginDetailsStatus)) {
            return loginDetailsStatus;
        }

        final LoginDetailsResponseDto loginDetailsResponseDto =
                loginDetailsResponse.getLoginDetailsResponseDto();

        if (isOtpRequired(loginDetailsResponseDto)) {
            otpDataStorage.storeInfo(loginDetailsResponseDto.getOtpChallenge().getOtpInfo());

            log.info("OTP is required.");
            return GetLoginDetailsStatus.OTP_REQUIRED;
        }

        return GetLoginDetailsStatus.LOGGED_IN;
    }

    private static GetLoginDetailsStatus validateLoginDetailsResponse(
            LoginDetailsResponse loginDetailsResponse) {
        if (!loginDetailsResponse.isSuccessful()) {
            return GetLoginDetailsStatus.ERROR_IN_RESPONSE;
        }

        final LoginDetailsResponseDto loginDetailsResponseDto =
                loginDetailsResponse.getLoginDetailsResponseDto();

        if (isPasswordChangeRequired(loginDetailsResponseDto)) {
            log.error("Password change is required.");
            return GetLoginDetailsStatus.PASSWORD_CHANGE_REQUIRED;
        }

        if (isAccountLocked(loginDetailsResponseDto)) {
            log.error("Account is locked.");
            return GetLoginDetailsStatus.ACCOUNT_LOCKED;
        }

        if (mustAcceptTerms(loginDetailsResponseDto)) {
            log.error("User must accept terms in other channel.");
            return GetLoginDetailsStatus.MUST_ACCEPT_TERMS;
        }

        return null;
    }

    private static boolean isOtpRequired(LoginDetailsResponseDto loginDetailsResponseDto) {
        return loginDetailsResponseDto.getOtpChallenge().isOtpRequired();
    }

    private static boolean isPasswordChangeRequired(
            LoginDetailsResponseDto loginDetailsResponseDto) {
        return Objects.nonNull(loginDetailsResponseDto.getUserAccountInfo())
                && loginDetailsResponseDto.getUserAccountInfo().isPasswordUpdateRequired();
    }

    private static boolean isAccountLocked(LoginDetailsResponseDto loginDetailsResponseDto) {
        return Objects.nonNull(loginDetailsResponseDto.getUserAccountInfo())
                && loginDetailsResponseDto.getUserAccountInfo().isAccountLocked();
    }

    private static boolean mustAcceptTerms(LoginDetailsResponseDto loginDetailsResponseDto) {
        return Objects.nonNull(loginDetailsResponseDto.getTermsAcceptanceInfo())
                && loginDetailsResponseDto.getTermsAcceptanceInfo().isMustAcceptTerms();
    }
}
