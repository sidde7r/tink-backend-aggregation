package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.HeaderValues;
import se.tink.integration.webdriver.WebDriverInitializer;

public abstract class SeleniumCommands {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int MAX_WAITING_TIME = 20000;
    protected WebDriver driver;

    public SeleniumCommands() {
        driver = getDriverInstance();
    }

    /** Close the current WebDriver */
    public void closeWebDriver() {
        try {
            driver.close();
            log.info("closed");
        } catch (Exception e) {
            log.error("Exception to run driver.close(");
            killProcesses();
        }
    }

    /** Close all the WebDriver instances */
    public void quitWebDriver() {
        try {
            driver.quit();
            driver = null;
            pause(5000);
        } catch (Exception e) {
            log.error("Exception to run driver.quit()");
            killProcesses();
        }
    }

    private void killProcesses() {
        try {
            Runtime.getRuntime().exec("taskkill /F /IM plugin-container.exe");
            pause(1000);
            log.error("Killing process plugin-container");
            Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");
            log.error("Killing process WerFault");
            pause(1000);
        } catch (Exception e2) {
            log.error(e2.getMessage());
        }
    }

    private WebDriver getDriverInstance() {

        WebDriver createdDriver =
                WebDriverInitializer.constructWebDriver(
                        HeaderValues.USER_AGENT, HeaderValues.ACCEPT_LANGUAGE);
        createdDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        return createdDriver;
    }

    public void pause(int time) {
        if (time > MAX_WAITING_TIME) time = MAX_WAITING_TIME;
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void waitForLoad() {
        ExpectedCondition<Boolean> pageLoadCondition =
            drive -> ((JavascriptExecutor) drive)
                    .executeScript("return document.readyState")
                    .equals("complete");

        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(pageLoadCondition);
        log.info("Page has been load successfully");
    }

    public void changeToFrame(String frameName) {
        driver.switchTo().frame(frameName);
        log.info("Change frame");
    }
}
