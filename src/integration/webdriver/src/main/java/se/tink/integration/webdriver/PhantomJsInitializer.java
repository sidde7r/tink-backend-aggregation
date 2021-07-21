package se.tink.integration.webdriver;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PhantomJsInitializer {

    private static final LogTag LOG_TAG = LogTag.from("[PhantomJsDriver]");

    public static WebDriverWrapper constructWebDriver(PhantomJsConfig config) {
        String driverId = UUID.randomUUID().toString();
        log.info("{} Starting PhantomJsDriver: {}", LOG_TAG, driverId);

        PhantomJSDriver driver = getPhantomJsDriver(config);

        return WebDriverWrapper.builder()
                .driver(driver)
                .javascriptExecutor(driver)
                .driverId(driverId)
                .build();
    }

    private static PhantomJSDriver getPhantomJsDriver(PhantomJsConfig config) {
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
                config.getAcceptLanguage());

        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent",
                config.getUserAgent());

        PhantomJSDriver driver = new PhantomJSDriver(capabilities);
        driver.manage().timeouts().pageLoadTimeout(config.getTimeoutInSeconds(), TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(config.getTimeoutInSeconds(), TimeUnit.SECONDS);
        return driver;
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
