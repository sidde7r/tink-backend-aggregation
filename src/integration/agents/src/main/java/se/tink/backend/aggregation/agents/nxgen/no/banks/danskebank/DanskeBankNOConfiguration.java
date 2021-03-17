package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

public class DanskeBankNOConfiguration implements DanskeBankConfiguration {
    private static final String APP_CULTURE = "nb-NO";
    private static final String APP_NAME = "com.danskebank.mobilebank3no";
    private static final String APP_REFERER = "MobileBanking3 NO";
    private static final String BRAND = "FOK";
    private static final String LANGUAGE_CODE = "NB";
    private static final String MARKET_CODE = "NO";
    private static final String DEVICE_SERIAL_NO_KEY = "x-device-serial-no";
    private static final String STEP_UP_TOKEN_KEY = "x-stepup-token";
    private static final String CLIENT_ID = "5ec4b8ad-a93d-43e1-831c-8e78ee6e661a";
    private static final String CLIENT_SECRET =
            "oT5hmRd2otSjlbrPAKtvmBJ5cTSVzwttGRxYLUzHfRvWS5m2Kf";
    private static final String APP_VERSION_HEADER =
            "MobileBank ios com danskebank.mobilebank3no 28076";
    private static final String APP_VERSION = "2021.2";
    private static final String USER_AGENT =
            "nymobilbanknodanskebank/2021.2 (com.danskebank.mobilebank3no; build:28076; iOS 13.3.1; NO)";

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
        return true;
    }

    @Override
    public List<String> getCheckingAccountTypes() {
        return ImmutableList.<String>builder()
                .add("1AP")
                .add("1LA")
                .add("1AJ")
                .add("1AK")
                .add("1AL")
                .add("1NC")
                .add("1BE")
                .add("1BY")
                .build();
    }

    @Override
    public List<String> getSavingsAccountTypes() {
        return ImmutableList.<String>builder()
                .add("1BU")
                .add("1B3")
                .add("1LB")
                .add("100")
                .add("1NE")
                .add("1B1")
                .add("1BB")
                .add("1NG")
                .build();
    }

    @Override
    public Map<String, LoanDetails.Type> getLoanAccountTypes() {
        return ImmutableMap.<String, LoanDetails.Type>builder()
                .put("1FV", LoanDetails.Type.MORTGAGE)
                .put("1LC", LoanDetails.Type.MORTGAGE)
                .put("1LJ", LoanDetails.Type.MORTGAGE)
                .put("1FP", LoanDetails.Type.MORTGAGE)
                .put("1LD", LoanDetails.Type.MORTGAGE)
                .put("1F9", LoanDetails.Type.MORTGAGE)
                .build();
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
        return DanskeBankConstants.SecuritySystem.SERVICE_CODE_SC;
    }

    @Override
    public Optional<String> getBindDeviceSecuritySystem() {
        return Optional.of(DanskeBankConstants.SecuritySystem.SERVICE_CODE_BD);
    }
}
