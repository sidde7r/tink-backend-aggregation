package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.system.rpc.Loan;

public class DanskeBankSEConfiguration implements DanskeBankConfiguration {
    // == START Standard configuration ==
    private static final String APP_CULTURE = "sv-SE";
    private static final String APP_NAME = "com.danskebank.mobilebank3se";
    private static final String APP_REFERER = "MobileBanking3 SE";
    private static final String APP_VERSION = "0.41.0";
    private static final String BRAND = "OEB";
    private static final String LANGUAGE_CODE = "SV";
    private static final String MARKET_CODE = "SE";
    private static final String SECURITY_SYSTEM = "SV";
    private static final String CLIENT_ID = "5ec4b8ad-a93d-43e1-831c-8e78ee6e661a";
    private static final String CLIENT_SECRET = "NRRM1W2ckjUdBwhbHtP38yIZevM9yr46v0wosfIWM4sYSFuCNy";
    private static final String APP_VERSION_HEADER = "MobileBank ios SE 1121105";
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

    public String getSecuritySystem() {
        return SECURITY_SYSTEM; // Bank ID Security system
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
}
