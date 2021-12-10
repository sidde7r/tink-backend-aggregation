package se.tink.integration.webdriver;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

@Getter
@Builder
public class WebDriverWrapperImpl implements WebDriverWrapper {

    @Delegate private final WebDriver driver;
    @Delegate private final JavascriptExecutor javascriptExecutor;
    private final String driverId;
}
