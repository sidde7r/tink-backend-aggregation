package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OtpInfoDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.data.ExchangeOtpCodeStatus;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class AktiaOtpDataStorage {

    private static final String TMP_OTP_INFO_STORAGE_KEY = "tmp_otp_info";
    private static final String TMP_OTP_CODE_STATUS_STORAGE_KEY = "tmp_otp_code_status";

    private final SessionStorage sessionStorage;

    public Optional<OtpInfoDto> getInfo() {
        return sessionStorage.get(TMP_OTP_INFO_STORAGE_KEY, OtpInfoDto.class);
    }

    public void storeInfo(OtpInfoDto otpInfo) {
        sessionStorage.put(TMP_OTP_INFO_STORAGE_KEY, otpInfo);
    }

    public Optional<ExchangeOtpCodeStatus> getStatus() {
        return sessionStorage.get(TMP_OTP_CODE_STATUS_STORAGE_KEY, ExchangeOtpCodeStatus.class);
    }

    public void storeStatus(ExchangeOtpCodeStatus status) {
        sessionStorage.put(TMP_OTP_CODE_STATUS_STORAGE_KEY, status);
    }
}
