package se.tink.integration.webdriver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeDriverInitializer {
    private static final String CHROMEDRIVER_PATH = "external/chromedriver/file/chromedriver/";
    private static final String BASE_CHROME_PATH = "external/chromium/file/chromium/";

    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36";
    private static final String DEFAULT_ACCEPT_LANGUAGE = "en-US";
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;

    private static boolean isMacOs = System.getProperty("os.name").toLowerCase().contains("mac");

    public static ChromeDriver constructChromeDriver() {
        return constructChromeDriver(DEFAULT_USER_AGENT, DEFAULT_ACCEPT_LANGUAGE);
    }

    public static ChromeDriver constructChromeDriver(String userAgent) {
        return constructChromeDriver(userAgent, DEFAULT_ACCEPT_LANGUAGE);
    }

    public static ChromeDriver constructChromeDriver(String userAgent, String acceptLanguage) {
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments(getListArguments(userAgent, acceptLanguage));
        options.setBinary(getChromePath());
        ChromeDriver driver = new ChromeDriver(options);

        driver.manage().timeouts().pageLoadTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        return new ChromeDriver(options);
    }

    public static List<String> getListArguments(String userAgent, String acceptLanguage) {
        List<String> arguments = new ArrayList<>();
        arguments.add("--no-sandbox");
        arguments.add("--ignore-certificate-errors");
        arguments.add("--ignore-ssl-errors");
        arguments.add("--user-agent=" + userAgent);
        arguments.add("--enable-javascript");
        arguments.add("--disable-extensions");
        arguments.add("--disable-infobars");
        arguments.add("--disable-dev-shm-usage");
        arguments.add("start-maximized");
        arguments.add("--allow-running-insecure-content");
        arguments.add("--lang=" + acceptLanguage);

        // if head mode needed for local development, just comment out two lines below.
        // Unfortunately doesn't work for linux. Remember to uncomment when pushing on prod.
        arguments.add("--headless");
        arguments.add("--blink-settings=imagesEnabled=false");
        return arguments;
    }

    public static void quitChromeDriver(WebDriver driver) {
        driver.close();
        driver.quit();
    }

    private static String getChromePath() {
        if (isMacOs) {
            return BASE_CHROME_PATH + "chromium";
        } else {
            return BASE_CHROME_PATH + "chrome";
        }
    }
}
