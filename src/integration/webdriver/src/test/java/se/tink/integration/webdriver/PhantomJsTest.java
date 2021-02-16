package se.tink.integration.webdriver;

import java.io.File;
import java.util.Map;
import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class PhantomJsTest {

    @Test
    public void whenFailingToFindPhantomJsExecutable_raiseCannotFindExecutableException() {
        try {
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
        } catch (IllegalStateException e) {
            assert e.getMessage().contains("The path to the driver executable must");
        }
    }

    @Test
    public void whenSuccessfullyFindingPhantomJsExecutable_canExecuteIt() {

        final String OS_TYPE = System.getProperty("os.name").toLowerCase();
        final String USER_AGENT =
                "Mozilla/5.0 (iPhone; CPU iPhone OS 10_1_1 like Mac OS X) AppleWebKit/602.2.14 (KHTML, like Gecko) Mobile/14B100";

        File phantomJsFile;
        if (OS_TYPE.contains("mac")) {
            phantomJsFile = new File("tools/phantomjs-tink-mac64-2.1.1");
        } else if (OS_TYPE.contains("linux")) {
            phantomJsFile = new File("tools/phantomjs-tink-linux-x86_64-2.1.1");
        } else {
            throw new IllegalStateException("Unsupported operating system");
        }

        final DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                phantomJsFile.getAbsolutePath());

        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
        capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS, false);

        String[] phantomArgs =
                new String[] {
                    "--web-security=false", "--load-images=false",
                };
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", USER_AGENT);

        new PhantomJSDriver(capabilities);
    }
}
