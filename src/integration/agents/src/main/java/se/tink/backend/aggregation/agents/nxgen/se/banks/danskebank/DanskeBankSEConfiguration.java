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
        // Documenting here what the product codes mean.
        // Commenting is done on the form:
        // <accountProduct>: "<accountName>" - "<'translated' accountName>"
        // Some accounts seem to have attributes like a checking account,
        // for instance: access to credit and debit card, but in the bank app, they are usually
        // displayed as savings accounts.
        // For consistency reasons, we'll map those as they are mapped in the bank app.
        return ImmutableList.<String>builder()
                // 2AA: "Byggnadskredit" - "Building credit"
                .add("2AA")
                // 2AE: Missing logs for this product code
                .add("2AE")
                // 2AF: "Företagskonto" - "Company account"
                .add("2AF")
                // 2A3: "Mastercard Guld" - "Mastercard Gold"
                .add("2A3")
                // 2A4: "Företagskort" - "Company card"
                .add("2A4")
                // 2BA: "Finanskonto" - "Finance account"
                .add("2BA")
                // 2BM: "Privatkonto special" - "Private account special"
                .add("2BM")
                // 2B4: "Danske start" - "Danske start account"
                .add("2B4")
                // 2B5: "Servicekonto" - "Service account"
                .add("2B5")
                // 2B6: "Baskonto" - "Base account"
                .add("2B6")
                // 2B7: "Danske Exklusiv" - "Danske Exclusive"
                .add("2B7")
                // 2B8: "Mastercard Platinum"
                .add("2B8")
                // 2B9: "World Elite" - Credit card
                .add("2B9")
                // 2B0: "Danske Plus" - Credit card
                .add("2B0")
                // 2CY: "Mastercard Direkt" - "Mastercard Direct"
                .add("2CY")
                // 2CZ: "Mastercard Direkt Ung" - "Mastercard Direct Young"
                .add("2CZ")
                // 2DE: "Gårdskonto" - "Farm account" with access to credit.
                .add("2DE")
                // 2DI: "June" - Account with access to credit
                .add("2DI")
                // 2EX: "Danske konto" - "Danske account"
                .add("2EX")
                // 3BG: "Danske Business ONE"
                .add("3BG")
                // 3BH: "Danske Business PLUS"
                .add("3BH")
                // 62C: "Valutakonto" - "Currency account"
                .add("62C")
                .build();
    }

    @Override
    public List<String> getSavingsAccountTypes() {
        return ImmutableList.<String>builder()
                // 2AB: "Depåkonto" - "Depot account"
                .add("2AB")
                // 2AN: "Specialinlåning" - "Special deposits"
                .add("2AN")
                // 2BP: "Sparkonto" - "Savings account".
                // An account with access to both credit and debit card.
                // However, many users have named this account personally and it's by default named
                // as "Savings account" from the bank.
                // It's most likely a savings account at Danske and we should map it accordingly.
                .add("2BP")
                // 2BS: "Skogskonto" - "Forest account"
                .add("2BS")
                // 2CF: "Indivudellt pensionssparkonto" - "Individual pension account"
                .add("2CF")
                // 2C2: "Sparkonto XL" - "Savings account XL"
                .add("2C2")
                // 2DC: "Investeringssparkonto"/"Individuellt sparkonto"
                // - "Investment savings account/Individual savings account"
                .add("2DC")
                // 2DD: "Investeringssparkonto val." - "Investment savings account"
                .add("2DD")
                // 2DH: "AKP ISK" - "Investment savings account"
                .add("2DH")
                // 2ED: "Fastränteplacering" - "Fixed-rate investment"
                .add("2ED")
                // 2EH: "<USERNAME> Spar" - Savings account that the user has named.
                .add("2EH")
                // 2EK: "Sparkonto XL Kampanj" - "Savings account XL campaign"
                .add("2EK")
                // 2SF: "Sparkonto Företag" - Savings account Company"
                .add("2SF")
                // 3CA: "HSB Bosparkonto" - "HSB home-saving account"
                .add("3CA")
                // 3CB: "HSB Fasträntekonto" - "HSB fixed-rate account"
                .add("3CB")
                .build();
    }

    @Override
    public Map<String, LoanDetails.Type> getLoanAccountTypes() {
        return ImmutableMap.<String, LoanDetails.Type>builder()
                // 2AT: "Företagslån (02)" - "Company loan"
                .put("2AT", LoanDetails.Type.OTHER)
                // 2AS: "Skogslån" - "Forest loan"
                .put("2AS", LoanDetails.Type.LAND)
                // 2CK: "Privatbostadslån" - "Private home loan"
                .put("2CK", LoanDetails.Type.MORTGAGE)
                // 2CL: "Privatlån" - "Privateloan"
                .put("2CL", LoanDetails.Type.BLANCO)
                // 2C6: "Direktlån" - "Direct loan"
                .put("2C6", LoanDetails.Type.OTHER)
                // 2DG: "Fasträntelån" - "Fixed-rate loan"
                .put("2DG", LoanDetails.Type.OTHER)
                // 2DL: "Handpenning/Överbryggningslån" - "Down payment/Bridging loan"
                .put("2DL", LoanDetails.Type.OTHER)
                // 3AC: "Bolån" - "Mortgage loan"
                .put("3AC", LoanDetails.Type.MORTGAGE)
                // 3AN: "Bolån Premium" - "Mortgage loan Premium"
                .put("3AN", LoanDetails.Type.MORTGAGE)
                // 3AS: "Företagslån (03)" - "Company loan"
                .put("3AS", LoanDetails.Type.OTHER)
                // 3AT: "Företagslån (04)" - "Company loan"
                .put("3AT", LoanDetails.Type.OTHER)
                // 3BJ: "Bolån Fast Hypotek" - "Mortgage loan fixed
                .put("3BJ", LoanDetails.Type.MORTGAGE)
                // 3BL: "Bolån Online Hypotek" - "Mortgage loan online"
                .put("3BL", LoanDetails.Type.MORTGAGE)
                // 3BM: "Bolån" - Mortgage loan"
                .put("3BM", LoanDetails.Type.MORTGAGE)
                // 3BK: "Bolån Premium Hypotek" - Mortgage loan Premium"
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
