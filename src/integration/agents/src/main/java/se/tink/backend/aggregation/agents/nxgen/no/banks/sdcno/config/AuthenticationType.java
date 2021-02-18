package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config;

import org.openqa.selenium.WebDriver;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers.EikaBankIdMobilInitializer;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers.PortalBankIdMobilInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.initializer.BankIdMobilInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.initializer.MobilInitializer;
import se.tink.integration.webdriver.WebDriverHelper;

public enum AuthenticationType {
    NETTBANK(Key.DATE_OF_BIRTH) {
        @Override
        public MobilInitializer getMobilBankIdInitializer(
                String mobileNumber,
                String idNumber,
                WebDriver driver,
                WebDriverHelper webDriverHelper) {
            return new BankIdMobilInitializer(mobileNumber, idNumber, driver, webDriverHelper);
        }
    },

    PORTAL(Key.NATIONAL_ID_NUMBER) {
        @Override
        public MobilInitializer getMobilBankIdInitializer(
                String mobileNumber,
                String idNumber,
                WebDriver driver,
                WebDriverHelper webDriverHelper) {
            return new PortalBankIdMobilInitializer(
                    mobileNumber, idNumber, driver, webDriverHelper);
        }
    },

    EIKA(Key.DATE_OF_BIRTH) {
        @Override
        public MobilInitializer getMobilBankIdInitializer(
                String mobileNumber,
                String idNumber,
                WebDriver driver,
                WebDriverHelper webDriverHelper) {
            return new EikaBankIdMobilInitializer(mobileNumber, idNumber, driver, webDriverHelper);
        }
    };

    private Key credentialsAdditionalKey;

    AuthenticationType(Key idNumberType) {
        this.credentialsAdditionalKey = idNumberType;
    }

    public Key getCredentialsAdditionalKey() {
        return credentialsAdditionalKey;
    }

    public abstract MobilInitializer getMobilBankIdInitializer(
            String mobileNumber,
            String idNumber,
            WebDriver driver,
            WebDriverHelper webDriverHelper);
}
