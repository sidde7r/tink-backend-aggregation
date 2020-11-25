package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;

public class DanskeBankDKConfiguration implements DanskeBankConfiguration {
    private static final String APP_CULTURE = "en-GB";
    private static final String APP_NAME = "com.danskebank.mobilebank3dk";
    private static final String APP_REFERER = "MobileBanking3 DK";
    private static final String APP_VERSION = "0.43.0";
    private static final String BRAND = "DB";
    private static final String LANGUAGE_CODE = "DA";
    private static final String MARKET_CODE = "DK";
    private static final String APP_VERSION_HEADER = "MobileBank ios DK 17798";
    private static final String DEVICE_SERIAL_NO_KEY = "x-device-serial-no";
    private static final String STEP_UP_TOKEN_KEY = "x-stepup-token";
    private static final String CLIENT_ID = "5ec4b8ad-a93d-43e1-831c-8e78ee6e661a";
    private static final String CLIENT_SECRET =
            "Z2ow2vugJe6RhSUpjhFoBFVdy8W41rnqs5JGJaLtrllCeJxrBF";

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
                .add("13X")
                .add("19C")
                .add("15C")
                .add("12G")
                .add("008")
                .add("009")
                .add("136")
                .add("100")
                .add("16E")
                .add("16K")
                .add("016")
                .build();
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
                .add("12K")
                .add("007")
                .add("542")
                .add("471")
                .build();
    }

    @Override
    public AccountCapabilities.Answer canExecuteExternalTransfer(String productCode) {
        return ImmutableMap.<String, AccountCapabilities.Answer>builder()
                // log-in via www to the bank and under account check that it has option to make
                // transfers (incl international transfers)
                .put("13X", AccountCapabilities.Answer.YES)
                .put("055", AccountCapabilities.Answer.NO)
                .put("12J", AccountCapabilities.Answer.YES)
                .put("19C", AccountCapabilities.Answer.YES)
                .build()
                .getOrDefault(productCode, AccountCapabilities.Answer.UNKNOWN);
    }

    @Override
    public AccountCapabilities.Answer canReceiveExternalTransfer(String productCode) {
        return ImmutableMap.<String, AccountCapabilities.Answer>builder()
                // see transfer history & and saved recipients/payees
                .put("12J", AccountCapabilities.Answer.YES)
                .put("055", AccountCapabilities.Answer.YES)
                .put("13X", AccountCapabilities.Answer.YES)
                .put("19C", AccountCapabilities.Answer.YES)
                .build()
                .getOrDefault(productCode, AccountCapabilities.Answer.UNKNOWN);
    }

    @Override
    public AccountCapabilities.Answer canPlaceFunds(String productCode) {
        return ImmutableMap.<String, AccountCapabilities.Answer>builder()
                // our current understanding is that canPlaceFunds is fulfilled if one of the
                // following is true:
                // - canReceiveExternalTransfer is true or
                // - you can make a physical deposit at a bank office or by depositing through a
                // depositing box/machine
                //
                // see saved recipients/payees to see confirm which accounts can receive transfer
                // directly
                .put("13X", AccountCapabilities.Answer.YES)
                .put("055", AccountCapabilities.Answer.YES)
                .put("12J", AccountCapabilities.Answer.YES)
                .put("19C", AccountCapabilities.Answer.YES)
                .build()
                .getOrDefault(productCode, AccountCapabilities.Answer.UNKNOWN);
    }

    @Override
    public AccountCapabilities.Answer canWithdrawCash(String productCode) {
        return ImmutableMap.<String, AccountCapabilities.Answer>builder()
                // log-in via www to the bank and see:
                // Card & currency -> Card overview -> Card limits -> info about limits for ATM
                .put("13X", AccountCapabilities.Answer.YES)
                .put("055", AccountCapabilities.Answer.YES)
                .put("12J", AccountCapabilities.Answer.YES)
                .put("19C", AccountCapabilities.Answer.YES)
                .build()
                .getOrDefault(productCode, AccountCapabilities.Answer.UNKNOWN);
    }

    @Override
    public Map<String, Loan.Type> getLoanAccountTypes() {
        return ImmutableMap.<String, Loan.Type>builder()
                .put("155", Loan.Type.MORTGAGE)
                .put("165", Loan.Type.MORTGAGE)
                .put("80X", Loan.Type.MORTGAGE)
                .put("16L", Loan.Type.BLANCO)
                .put("094", Loan.Type.VEHICLE)
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
        // No SecuritySystem on `bindDevice`.
        return Optional.empty();
    }
}
