package se.tink.integration.webdriver;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public interface WebDriverWrapper extends WebDriver, JavascriptExecutor {

    WebDriver getDriver();

    JavascriptExecutor getJavascriptExecutor();

    String getDriverId();
}
