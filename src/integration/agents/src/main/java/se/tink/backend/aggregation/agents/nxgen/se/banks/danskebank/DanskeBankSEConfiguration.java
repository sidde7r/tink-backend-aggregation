package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private static final String CLIENT_SECRET = "Et7TLuUT6WfZbz9RLlwYKUQMVK99Gq4yU3FZqsebMO7MHFtSZ2";
    private static final String APP_VERSION_HEADER = "MobileBank ios SE 1315097";
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
        return Collections.emptyList();
    }

    @Override
    public List<String> getSavingsAccountTypes() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Loan.Type> getLoanAccountTypes() {
        return Collections.emptyMap();
    }

    public class BankIdStatus {
        public static final String ALREADY_IN_PROGRESS = "already_in_progress";
        public static final String CANCELLED = "cancelled";
        public static final String NO_CLIENT = "no_client";
        public static final String OK = "ok";
        public static final String OUTSTANDING_TRANSACTION = "outstanding_transaction";
        public static final String USER_CANCEL = "user_cancel";
        public static final String USER_SIGN = "user_sign";
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
}
