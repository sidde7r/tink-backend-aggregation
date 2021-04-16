package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping;

import org.openqa.selenium.By;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;

public class WebScrapingConstants {
    public static final String USER_AGENT =
            DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getMozillaVersion()
                    + " "
                    + DeviceProfileConfiguration.IOS_STABLE
                            .getUserAgentEntity()
                            .getSystemAndBrowserInfo()
                    + " "
                    + DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getPlatform()
                    + " "
                    + DeviceProfileConfiguration.IOS_STABLE
                            .getUserAgentEntity()
                            .getPlatformDetails()
                    + " "
                    + DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getExtensions();

    public static class Xpath {
        public static final By USERNAME_XPATH = By.xpath("//form//input[@maxlength='11']");
        public static final By FORM_XPATH = By.xpath("//form");
        public static final By PASSWORD_INPUT_XPATH =
                By.xpath("//form//input[@type='password'][@maxlength]");
        public static final By MOBILE_BANK_ID_INPUT_XPATH =
                By.xpath("//form//input[@type='password'][@disabled='true'][@pattern='[0-9]*']");
        public static final By BANK_ID_PASSWORD_INPUT_XPATH =
                By.xpath("//form//input[@data-type='password']");
        public static final By BANK_ID_APP_TITLE_XPATH =
                By.xpath("//div[@class='title label' and h2[contains(text(), 'BankID-app')]]");
        public static final By AUTHENTICATION_LIST_BUTTON_XPATH =
                By.xpath("//button[@class='link' and span[contains(text(),'BankID')]]");
        public static final By BANK_ID_MOBIL_BUTTON =
                By.xpath(
                        "//ul/child::li/child::button[span[contains(text(),'mobil') and contains(text(),'BankID')]]");
        public static final By REFERENCE_WORDS_XPATH =
                By.xpath("//span[@data-bind='text: reference' and text()!='']");
    }
}
