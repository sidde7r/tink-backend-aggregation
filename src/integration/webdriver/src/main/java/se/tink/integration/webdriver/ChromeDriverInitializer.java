package se.tink.integration.webdriver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorageItem;

/**
 * Remember to always use initialization methods with {@link AgentTemporaryStorage} parameter in
 * production code.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChromeDriverInitializer {

    private static final LogTag LOG_TAG = LogTag.from("[ChromeDriver]");

    private static final String CHROMEDRIVER_PATH =
            "external/chromedriver/file/chromedriver/chromedriver";
    private static final String BASE_CHROME_PATH = "external/chromium/file/chromium/";
    private static final String MAC_CHROME_PATH =
            BASE_CHROME_PATH + "chrome-mac/Chromium.app/Contents/MacOS/Chromium";
    private static final String LINUX_CHROME_PATH = BASE_CHROME_PATH + "chrome-linux/chrome";

    private static final boolean IS_MAC_OS =
            System.getProperty("os.name").toLowerCase().contains("mac");

    public static WebDriverWrapper constructChromeDriver(
            AgentTemporaryStorage agentTemporaryStorage) {
        return constructChromeDriver(ChromeDriverConfig.defaultConfig(), agentTemporaryStorage);
    }

    public static WebDriverWrapper constructChromeDriver(
            ChromeDriverConfig config, AgentTemporaryStorage agentStorage) {
        WebDriverWrapper driverWrapper = constructChromeDriver(config);
        saveInAgentStorage(driverWrapper, agentStorage);
        return driverWrapper;
    }

    public static WebDriverWrapper constructChromeDriver() {
        return constructChromeDriver(ChromeDriverConfig.defaultConfig());
    }

    public static WebDriverWrapper constructChromeDriver(ChromeDriverConfig config) {
        String driverId = UUID.randomUUID().toString();
        log.info("{} Starting ChromeDriver: {}", LOG_TAG, driverId);

        ChromeDriver driver = startChromeDriver(config);

        return WebDriverWrapper.builder()
                .driver(driver)
                .javascriptExecutor(driver)
                .driverId(driverId)
                .build();
    }

    private static void saveInAgentStorage(
            WebDriverWrapper driverWrapper, AgentTemporaryStorage agentStorage) {
        agentStorage.save(
                driverWrapper.getDriverId(),
                AgentTemporaryStorageItem.<WebDriverWrapper>builder()
                        .item(driverWrapper)
                        .itemCleaner(ChromeDriverInitializer::quitChromeDriver)
                        .build());
    }

    private static ChromeDriver startChromeDriver(ChromeDriverConfig config) {
        File chromeFile = new File(getChromePath());
        log.info("chrome exists: " + chromeFile.exists());
        log.info("chrome path: " + chromeFile.getAbsolutePath());

        File chromeDriverFile = new File(CHROMEDRIVER_PATH);
        log.info("chromedriver exists: " + chromeDriverFile.exists());
        log.info("chromedriver path: " + chromeDriverFile.getAbsolutePath());
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_PATH);

        ChromeOptions options = buildChromeOptions(config);
        ChromeDriver driver = new ChromeDriver(options);

        driver.manage().timeouts().pageLoadTimeout(config.getTimeoutInSeconds(), TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(config.getTimeoutInSeconds(), TimeUnit.SECONDS);
        return driver;
    }

    private static ChromeOptions buildChromeOptions(ChromeDriverConfig config) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(getListArguments(config));
        options.setBinary(getChromePath());

        if (config.getProxy() != null) {
            DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability(CapabilityType.PROXY, config.getProxy());
            options.merge(desiredCapabilities);
        }

        return options;
    }

    static List<String> getListArguments(ChromeDriverConfig config) {
        List<String> arguments = new ArrayList<>();
        arguments.add("--no-sandbox");
        arguments.add("--ignore-certificate-errors");
        arguments.add("--ignore-ssl-errors");
        arguments.add("--user-agent=" + config.getUserAgent());
        arguments.add("--enable-javascript");
        arguments.add("--disable-extensions");
        arguments.add("--disable-infobars");
        arguments.add("--disable-dev-shm-usage");
        arguments.add("start-maximized");
        arguments.add("--allow-running-insecure-content");
        arguments.add("--lang=" + config.getAcceptLanguage());
        arguments.add("--remote-debugging-port=0");

        // if head mode needed for local development, just comment out two lines below.
        // Unfortunately doesn't work for linux. Remember to uncomment when pushing on prod.
        arguments.add("--headless");
        arguments.add("--blink-settings=imagesEnabled=false");

        return arguments;
    }

    public static void quitChromeDriver(WebDriverWrapper driver) {
        if (driver.getDriver() == null) {
            log.warn("{} Attempt to close uninitialized ChromeDriver", LOG_TAG);
            return;
        }
        log.info("{} Closing ChromeDriver: {}", LOG_TAG, driver.getDriverId());
        driver.close();
        driver.quit();
    }

    private static String getChromePath() {
        if (IS_MAC_OS) {
            return MAC_CHROME_PATH;
        } else {
            return LINUX_CHROME_PATH;
        }
    }
}
