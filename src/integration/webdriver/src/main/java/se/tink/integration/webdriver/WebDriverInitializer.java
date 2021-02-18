package se.tink.integration.webdriver;

import java.io.File;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WebDriverInitializer {

    private static final long DEFAULT_PHANTOMJS_TIMEOUT_SECONDS = 30;

    private static final String DEFAULT_ACCEPT_LANGUAGE = "en-US";
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 10_1_1 like Mac OS X) AppleWebKit/602.2.14 (KHTML, like Gecko) Mobile/14B100";

    public static WebDriver constructWebDriver() {
        return constructWebDriver(DEFAULT_USER_AGENT);
    }

    public static WebDriver constructWebDriver(String userAgent) {
        return constructWebDriver(userAgent, DEFAULT_ACCEPT_LANGUAGE);
    }

    public static WebDriver constructWebDriver(String userAgent, String acceptLanguage) {
        WebDriver driver = getPhantomJsDriver(userAgent, acceptLanguage);
        driver.manage()
                .timeouts()
                .pageLoadTimeout(DEFAULT_PHANTOMJS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        driver.manage()
                .timeouts()
                .setScriptTimeout(DEFAULT_PHANTOMJS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return driver;
    }

    private static WebDriver getPhantomJsDriver(String userAgent, String acceptLanguage) {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        File file = readDriverFile();
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, file.getAbsolutePath());

        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
        capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS, false);

        String[] phantomArgs =
                new String[] {
                    // To allow iframe-hacking
                    "--web-security=false",

                    // No need to load images
                    "--load-images=false",

                    // To enable debug:
                    // "--debug=false",
                    // "--webdriver-loglevel=DEBUG",
                    // "--webdriver-logfile=/tmp/phantomjs.log",
                    //
                    // To disable debug:
                    "--debug=false",

                    // To setup proxy
                    // "--proxy=127.0.0.1:8888",
                    // "--ignore-ssl-errors=true"
                };
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);

        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "Accept-Language",
                acceptLanguage);

        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", userAgent);

        return new PhantomJSDriver(capabilities);
    }

    private static File readDriverFile() {
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");

        if (mac) {
            return new File("tools/phantomjs-tink-mac64-2.1.1");
        } else {
            return new File("tools/phantomjs-tink-linux-x86_64-2.1.1");
        }
    }
}
