package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static java.util.Collections.emptyList;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_PRIVATE_PASSWORD_ERROR_BUBBLE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.WAIT_FOR_SIGN_THAT_AUTHENTICATION_IS_FINISHED_FOR_SECONDS;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdVerifyAuthenticationStep {

    private final BankIdWebDriver webDriver;
    private final BankIdScreensManager screensManager;

    public void verify() {
        /*
        Wait for any signs of successful or failed authentication. Check all conditions one by one for optimisation
        since we don't know which one can occur first.
         */
        for (int i = 0; i < WAIT_FOR_SIGN_THAT_AUTHENTICATION_IS_FINISHED_FOR_SECONDS; i++) {

            if (checkIfControllerLeftIframe()) {
                log.info("{} Controller left iframe - authentication finished", BANK_ID_LOG_PREFIX);
                return;
            }
            if (isPasswordErrorBubbleFound()) {
                throw BankIdNOError.INVALID_BANK_ID_PASSWORD.exception();
            }
            verifyNoErrorScreens();

            webDriver.sleepFor(1_000);
        }

        log.info(
                "{} Authentication did not finish - looking for any known screen",
                BANK_ID_LOG_PREFIX);
        BankIdScreen screen =
                screensManager.waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(BankIdScreen.getAllScreens())
                                .build());
        throw new IllegalStateException(
                String.format(
                        "%s Authentication did not finish. Current screen: %s",
                        BANK_ID_LOG_PREFIX, screen));
    }

    private boolean checkIfControllerLeftIframe() {
        return webDriver
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(LOC_IFRAME)
                                .searchOnlyOnce()
                                .build())
                .isEmpty();
    }

    private boolean isPasswordErrorBubbleFound() {
        return webDriver
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(LOC_PRIVATE_PASSWORD_ERROR_BUBBLE)
                                .searchForSeconds(2)
                                .build())
                .isNotEmpty();
    }

    private void verifyNoErrorScreens() {
        screensManager.tryWaitForAnyScreenFromQuery(
                BankIdScreensQuery.builder()
                        .waitForScreens(emptyList())
                        .waitForSeconds(0)
                        .verifyNoErrorScreens(true)
                        .build());
    }
}
