package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;

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
            "pPhT5uRfHtZAagpRxUBnB8nTpqaOzqpEcnc2Lr3mFCzr8SQ2yt";
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
                .add("2AB")
                .add("2AE")
                .add("2A3")
                .add("2A4")
                .add("2B4")
                .add("2EH")
                .add("2EX")
                .add("3BH")
                .build();
    }

    @Override
    public List<String> getSavingsAccountTypes() {
        return ImmutableList.<String>builder()
                .add("2BP")
                .add("2CF")
                .add("2C2")
                .add("2DC")
                .add("3CA")
                .build();
    }

    @Override
    public Map<String, Loan.Type> getLoanAccountTypes() {
        return ImmutableMap.<String, Loan.Type>builder().put("3BJ", Loan.Type.MORTGAGE).build();
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
