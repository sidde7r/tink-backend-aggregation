package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.system.rpc.Loan;

public class DanskeBankDKConfiguration implements DanskeBankConfiguration {
    private static final String APP_CULTURE = "en-GB";
    private static final String APP_NAME = "com.danskebank.mobilebank3dk";
    private static final String APP_REFERER = "MobileBanking3 DK";
    private static final String APP_VERSION = "0.41.0";
    private static final String BRAND = "DB";
    private static final String LANGUAGE_CODE = "DA";
    private static final String MARKET_CODE = "DK";
    private static final String CLIENT_ID = "5ec4b8ad-a93d-43e1-831c-8e78ee6e661a";
    private static final String CLIENT_SECRET = "NRRM1W2ckjUdBwhbHtP38yIZevM9yr46v0wosfIWM4sYSFuCNy";
    private static final String APP_VERSION_HEADER = "MobileBank ios DK 1120961";

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
                .add("542")
                .add("13X")
                .add("19C")
                .add("15C")
                .add("12G").build();
    }

    @Override
    public List<String> getSavingsAccountTypes() {
        return ImmutableList.<String>builder()
                .add("12J")
                .add("025")
                .add("134")
                .add("15G")
                .add("15M")
                .add("055")
                .add("15U")
                .add("655")
                .add("12K").build();
    }

    @Override
    public Map<String, Loan.Type> getLoanAccountTypes() {
        return ImmutableMap.<String, Loan.Type>builder()
                .put("155", Loan.Type.MORTGAGE)
                .put("165", Loan.Type.MORTGAGE)
                .put("16L", Loan.Type.BLANCO)
                .put("094", Loan.Type.VEHICLE).build();
    }

}
