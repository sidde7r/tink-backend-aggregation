package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.xnap.commons.i18n.I18n;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.i18n.Catalog;

@RequiredArgsConstructor
public class DanskeBankDKConfiguration implements DanskeBankConfiguration {

    private final Catalog catalog;

    private static final String EN_APP_CULTURE = "en-DK";
    private static final String DA_APP_CULTURE = "da-DK";
    private static final String APP_NAME = "com.danskebank.mobilebank3dk";
    private static final String APP_REFERER = "MobileBanking3 DK";
    private static final String BRAND = "DB";
    private static final String DA_LANGUAGE_CODE = "DA";
    private static final String EN_LANGUAGE_CODE = "EN";
    private static final String MARKET_CODE = "DK";
    private static final String DEVICE_SERIAL_NO_KEY = "x-device-serial-no";
    private static final String STEP_UP_TOKEN_KEY = "x-stepup-token";
    private static final String CLIENT_ID = "5ec4b8ad-a93d-43e1-831c-8e78ee6e661a";
    private static final String APP_VERSION = "2021.1";
    private static final String APP_VERSION_HEADER =
            "MobileBank ios com danskebank.mobilebank3dk 28076";
    private static final String CLIENT_SECRET =
            "WYwlVohBVQhQ7KcMm3pP6aFcZkAE931qi8w4Y4ibH5l4EozN1t";
    private static final String USER_AGENT =
            "dennyemobilbankdkdanskebank/2021.2 (com.danskebank.mobilebank3dk; build:28076; iOS 13.3.1; DK)";

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
        return EN_LANGUAGE_CODE.equalsIgnoreCase(getLanguageCode())
                ? EN_APP_CULTURE
                : DA_APP_CULTURE;
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
        String userLanguageCode =
                Optional.ofNullable(catalog.getI18n())
                        .map(I18n::getLocale)
                        .map(Locale::getLanguage)
                        .orElse(DA_LANGUAGE_CODE);
        return EN_LANGUAGE_CODE.equalsIgnoreCase(userLanguageCode)
                ? EN_LANGUAGE_CODE
                : DA_LANGUAGE_CODE;
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
    public Map<String, LoanDetails.Type> getLoanAccountTypes() {
        return ImmutableMap.<String, LoanDetails.Type>builder()
                .put("155", LoanDetails.Type.MORTGAGE)
                .put("165", LoanDetails.Type.MORTGAGE)
                .put("80X", LoanDetails.Type.MORTGAGE)
                .put("16L", LoanDetails.Type.BLANCO)
                .put("094", LoanDetails.Type.VEHICLE)
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
