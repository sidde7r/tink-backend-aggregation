package se.tink.backend.aggregation.phantomjs;

import java.util.Map;
import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

public class PhantomJsTest {

    @Test
    public void whenAttemptingToLoadFirefoxDriver_raisePermissibleException() {
        final WebDriver driver =
                new PhantomJSDriver(
                        new Capabilities() {
                            @Override
                            public Map<String, Object> asMap() {
                                return null;
                            }

                            @Override
                            public Object getCapability(String s) {
                                return null;
                            }
                        });
    }
}
