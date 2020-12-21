package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

public class DanskeBankSEConfiguration implements DanskeBankConfiguration {
    // == START Standard configuration ==
    private static final String APP_CULTURE = "sv-SE";
    private static final String APP_NAME = "com.danskebank.mobilebank3se";
    private static final String APP_REFERER = "MobileBanking3 SE";
    private static final String APP_VERSION = "0.43.0";
    private static final String BRAND = "OEB";
    private static final String LANGUAGE_CODE = "SV";
    private static final String MARKET_CODE = "SE";
    private static final String SECURITY_SYSTEM = "SV";
    private static final String CLIENT_ID = "5ec4b8ad-a93d-43e1-831c-8e78ee6e661a";
    private static final String CLIENT_SECRET =
            "2cAZCUQGWxm6Eb11pRHBW7CeOgveV9A8cQivaviQHt5qCE156h";
    private static final String APP_VERSION_HEADER = "MobileBank ios SE 17798";
    private static final String DEVICE_SERIAL_NO_KEY = "x-device-serial-no";
    private static final String STEP_UP_TOKEN_KEY = "x-stepup-token";
    // == END Standard configuration ==

    @Override
    public String getAppVersionHeader() {
        return APP_VERSION_HEADER;
    }

    @Override
    public String getClientId() {
        return CLIENT_ID;
    }

    @Override
    public String getClientSecret() {
        return CLIENT_SECRET;
    }

    @Override
    public String getAppCulture() {
        return APP_CULTURE;
    }

    @Override
    public String getAppName() {
        return APP_NAME;
    }

    @Override
    public String getAppReferer() {
        return APP_REFERER;
    }

    @Override
    public String getAppVersion() {
        return APP_VERSION;
    }

    @Override
    public String getBrand() {
        return BRAND;
    }

    @Override
    public String getLanguageCode() {
        return LANGUAGE_CODE;
    }

    @Override
    public String getMarketCode() {
        return MARKET_CODE;
    }

    @Override
    public boolean shouldAddXAppCultureHeader() {
        return true;
    }

    @Override
    public List<String> getCheckingAccountTypes() {
        return ImmutableList.<String>builder()
                .add("2AA")
                .add("2AB")
                .add("2AE")
                .add("2AF")
                .add("2A3")
                .add("2A4")
                .add("2BA")
                .add("2BM")
                .add("2B4")
                .add("2B5")
                .add("2B6")
                .add("2B7")
                .add("2B8")
                .add("2B9")
                .add("2B0")
                .add("2CY")
                .add("2CZ")
                .add("2DE")
                .add("2EH")
                .add("2EX")
                .add("3BG")
                .add("3BH")
                .add("62C")
                .build();
    }

    @Override
    public List<String> getSavingsAccountTypes() {
        return ImmutableList.<String>builder()
                .add("2AN")
                .add("2BP")
                .add("2BS")
                .add("2CF")
                .add("2C2")
                .add("2DC")
                .add("2DD")
                .add("2DH")
                .add("2DI")
                .add("2ED")
                .add("2EK")
                .add("2SF")
                .add("3CA")
                .add("3CB")
                .build();
    }

    @Override
    public Map<String, LoanDetails.Type> getLoanAccountTypes() {
        return ImmutableMap.<String, LoanDetails.Type>builder()
                .put("2AT", LoanDetails.Type.OTHER)
                .put("2AS", LoanDetails.Type.LAND)
                .put("2CK", LoanDetails.Type.MORTGAGE)
                .put("2CL", LoanDetails.Type.BLANCO)
                .put("2C6", LoanDetails.Type.OTHER)
                .put("2DG", LoanDetails.Type.OTHER)
                .put("2DL", LoanDetails.Type.OTHER)
                .put("3AC", LoanDetails.Type.MORTGAGE)
                .put("3AN", LoanDetails.Type.MORTGAGE)
                .put("3AS", LoanDetails.Type.OTHER)
                .put("3AT", LoanDetails.Type.OTHER)
                .put("3BJ", LoanDetails.Type.MORTGAGE)
                .put("3BL", LoanDetails.Type.MORTGAGE)
                .put("3BM", LoanDetails.Type.MORTGAGE)
                .put("3BK", LoanDetails.Type.MORTGAGE)
                .build();
    }

    public class BankIdStatus {
        public static final String ALREADY_IN_PROGRESS = "already_in_progress";
        public static final String CANCELLED = "cancelled";
        public static final String NO_CLIENT = "no_client";
        public static final String OK = "ok";
        public static final String OUTSTANDING_TRANSACTION = "outstanding_transaction";
        public static final String USER_CANCEL = "user_cancel";
        public static final String USER_SIGN = "user_sign";
        public static final String EXPIRED_TRANSACTION = "expired_transaction";
    }

    @Override
    public String getStepUpTokenKey() {
        return STEP_UP_TOKEN_KEY;
    }

    @Override
    public String getDeviceSerialNumberKey() {
        return DEVICE_SERIAL_NO_KEY;
    }

    @Override
    public String getSecuritySystem() {
        return SECURITY_SYSTEM; // Bank ID Security system
    }

    @Override
    public Optional<String> getBindDeviceSecuritySystem() {
        // No SecuritySystem on `bindDevice`.
        return Optional.empty();
    }
}
