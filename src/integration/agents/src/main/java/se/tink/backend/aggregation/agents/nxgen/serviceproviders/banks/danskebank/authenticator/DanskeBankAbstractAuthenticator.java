package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;

import java.io.File;

public abstract class DanskeBankAbstractAuthenticator {
    private static final File phantomJsFile;

    static {
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");

        if (mac) {
            phantomJsFile = new File("tools/phantomjs-tink-mac64-2.1.1");
        } else {
            phantomJsFile = new File("tools/phantomjs-tink-linux-x86_64-1.9.8");
        }
    }

    protected WebDriver constructWebDriver() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                phantomJsFile.getAbsolutePath());

        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
        capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS, false);

        String[] phantomArgs = new String[] {
                // To allow iframe-hacking
                "--web-security=false",
                // No need to load images
                "--load-images=false",
                // For debugging, activate these:
                // "--webdriver-loglevel=DEBUG",
                // "--debug=true",
        };
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent",
                DanskeBankConstants.Javascript.USER_AGENT);
        return new PhantomJSDriver(capabilities);
    }

    protected abstract FinalizeAuthenticationResponse finalizeAuthentication();
}
