package se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

public class DanskeBankFIConfiguration implements DanskeBankConfiguration {
    private static final String APP_CULTURE = "en-GB";
    private static final String APP_NAME = "com.danskebank.mobilebank3fi";
    private static final String APP_REFERER = "MobileBanking3 FI";
    private static final String BRAND = "SAM";
    private static final String LANGUAGE_CODE = "FI";
    private static final String MARKET_CODE = "FI";
    private static final String DEVICE_SERIAL_NO_KEY = "x-device-serial-no";
    private static final String STEP_UP_TOKEN_KEY = "x-stepup-token";
    private static final String CLIENT_ID = "5ec4b8ad-a93d-43e1-831c-8e78ee6e661a";
    private static final String APP_VERSION = "2021.2";
    private static final String CLIENT_SECRET =
            "k49FSuV18VUl7YyMoE4yxtNfDSCJtmS6oTXraw1ucprL9HqJWc";
    private static final String APP_VERSION_HEADER =
            "MobileBank ios com danskebank.mobilebank3fi 28076";
    private static final String USER_AGENT =
            "uusimobiilipankkidanskebank/2021.2 (com.danskebank.mobilebank3fi; build:28076; iOS 13.3.1; FI)";

    public String getUserAgent() {
        return USER_AGENT;
    }

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
        return false;
    }

    @Override
    public List<String> getCheckingAccountTypes() {
        return ImmutableList.<String>builder()
                .add("7BC")
                .add("80X")
                .add("76P")
                .add("72R")
                .add("80D")
                .build();
    }

    @Override
    public List<String> getSavingsAccountTypes() {
        return ImmutableList.<String>builder()
                .add("70M")
                .add("76S")
                .add("76V")
                .add("71P")
                .add("79U")
                .build();
    }

    @Override
    public Map<String, LoanDetails.Type> getLoanAccountTypes() {
        return ImmutableMap.<String, LoanDetails.Type>builder()
                .put("7BE", LoanDetails.Type.BLANCO)
                .put("68A", LoanDetails.Type.BLANCO)
                .put("68P", LoanDetails.Type.BLANCO)
                .put("79G", LoanDetails.Type.MORTGAGE)
                .put("77P", LoanDetails.Type.MORTGAGE)
                .put("77S", LoanDetails.Type.STUDENT)
                .put("73D", LoanDetails.Type.STUDENT)
                .build();
    }

    public String getDeviceSerialNumberKey() {
        return DEVICE_SERIAL_NO_KEY;
    }

    public String getStepUpTokenKey() {
        return STEP_UP_TOKEN_KEY;
    }

    @Override
    public String getSecuritySystem() {
        return DanskeBankConstants.SecuritySystem.SERVICE_CODE_JS;
    }

    @Override
    public Optional<String> getBindDeviceSecuritySystem() {
        // No SecuritySystem on `bindDevice`.
        return Optional.empty();
    }
}
