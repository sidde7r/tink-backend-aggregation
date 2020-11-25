package se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;

public class DanskeBankFIConfiguration implements DanskeBankConfiguration {
    private static final String APP_CULTURE = "en-GB";
    private static final String APP_NAME = "com.danskebank.mobilebank3fi";
    private static final String APP_REFERER = "MobileBanking3 FI";
    private static final String APP_VERSION = "0.43.0";
    private static final String BRAND = "SAM";
    private static final String LANGUAGE_CODE = "FI";
    private static final String MARKET_CODE = "FI";
    private static final String DEVICE_SERIAL_NO_KEY = "x-device-serial-no";
    private static final String STEP_UP_TOKEN_KEY = "x-stepup-token";
    private static final String CLIENT_ID = "5ec4b8ad-a93d-43e1-831c-8e78ee6e661a";
    private static final String CLIENT_SECRET =
            "OOyMUa8VuMvkyRQZrnmNnNhsGkzpOu1yhtZw4eb5yki9c9Sr8l";
    private static final String APP_VERSION_HEADER = "MobileBank ios FI 17798";

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
    public Map<String, Loan.Type> getLoanAccountTypes() {
        return ImmutableMap.<String, Loan.Type>builder()
                .put("7BE", Loan.Type.BLANCO)
                .put("68A", Loan.Type.BLANCO)
                .put("68P", Loan.Type.BLANCO)
                .put("79G", Loan.Type.MORTGAGE)
                .put("77P", Loan.Type.MORTGAGE)
                .put("77S", Loan.Type.STUDENT)
                .put("73D", Loan.Type.STUDENT)
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
