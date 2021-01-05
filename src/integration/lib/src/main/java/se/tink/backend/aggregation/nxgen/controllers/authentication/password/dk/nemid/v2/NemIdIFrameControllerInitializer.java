package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.PHANTOMJS_TIMEOUT_SECONDS;

import java.io.File;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.metrics.NemIdMetricLabel;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps.NemIdCollectTokenStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps.NemIdInitializeIframeStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps.NemIdLoginPageStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps.NemIdVerifyLoginResponseStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps.NemIdWaitForCodeAppResponseStep;
import se.tink.libraries.i18n.Catalog;

/**
 * This class should be used to correctly initialize {@link
 * se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdIFrameController}
 * with all its dependencies.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NemIdIFrameControllerInitializer {

    public static NemIdIFrameController initNemIdIframeController(
            NemIdParametersFetcher nemIdParametersFetcher,
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalRequester supplementalRequester,
            MetricContext metricContext) {

        NemIdMetrics metrics = new NemIdMetrics(metricContext.getMetricRegistry());

        WebDriver driver =
                metrics.executeWithTimer(
                        NemIdIFrameControllerInitializer::constructWebDriver,
                        NemIdMetricLabel.WEB_DRIVER_CONSTRUCTION);
        NemIdWebDriverWrapper driverWrapper = new NemIdWebDriverWrapper(driver, new Sleeper());

        NemIdCredentialsStatusUpdater credentialsStatusUpdater =
                new NemIdCredentialsStatusUpdater(statusUpdater, catalog);
        NemIdTokenValidator tokenValidator = new NemIdTokenValidator(new NemIdTokenParser());

        NemIdInitializeIframeStep nemIdInitializeIframeStep =
                new NemIdInitializeIframeStep(
                        driverWrapper, metrics, credentialsStatusUpdater, nemIdParametersFetcher);
        NemIdLoginPageStep loginPageStep =
                new NemIdLoginPageStep(driverWrapper, credentialsStatusUpdater);
        NemIdVerifyLoginResponseStep verifyLoginResponseStep =
                new NemIdVerifyLoginResponseStep(
                        driverWrapper, metrics, credentialsStatusUpdater, tokenValidator);
        NemIdWaitForCodeAppResponseStep waitForCodeAppResponseStep =
                new NemIdWaitForCodeAppResponseStep(
                        driverWrapper,
                        metrics,
                        credentialsStatusUpdater,
                        catalog,
                        supplementalRequester);
        NemIdCollectTokenStep collectTokenStep =
                new NemIdCollectTokenStep(driverWrapper, metrics, tokenValidator);

        return new NemIdIFrameController(
                driverWrapper,
                metrics,
                nemIdInitializeIframeStep,
                loginPageStep,
                verifyLoginResponseStep,
                waitForCodeAppResponseStep,
                collectTokenStep);
    }

    private static WebDriver constructWebDriver() {
        WebDriver driver = getPhantomJsDriver();
        driver.manage().timeouts().pageLoadTimeout(PHANTOMJS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(PHANTOMJS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return driver;
    }

    private static WebDriver getPhantomJsDriver() {
        File file = readDriverFile();

        DesiredCapabilities capabilities = new DesiredCapabilities();

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
                    // For debugging, activate these:
                    // "--webdriver-loglevel=DEBUG",
                    "--debug=false",
                    // "--proxy=127.0.0.1:8888",
                    // "--ignore-ssl-errors=true",
                    // "--webdriver-logfile=/tmp/phantomjs.log"
                };

        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent",
                NemIdConstantsV2.USER_AGENT);

        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent",
                NemIdConstantsV2.USER_AGENT);

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
