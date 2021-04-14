package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BankIdScreensQuery {

    private final List<BankIdScreen> screensToWaitFor;
    private final int waitForSeconds;
    /**
     * Include all error screens in the list of screens to search for. Additionally, if such an
     * error screen is found, throw appropriate bank id exception. This is a convenient way to not
     * repeat throwing same exception each time when we want to search for some non error screen. It
     * also speeds up searching - whenever error screen occurs we will detect it in the very next
     * search iteration and stop unnecessarily waiting for other expected screens.
     */
    private final boolean shouldVerifyNoErrorScreens;

    public static BankIdWaitForScreenQueryBuilder builder() {
        return new BankIdWaitForScreenQueryBuilder();
    }

    public static class BankIdWaitForScreenQueryBuilder {

        private final List<BankIdScreen> screensToWaitFor = new ArrayList<>();
        private int searchForSeconds = BankIdConstants.DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT_IN_SECONDS;
        private boolean verifyNoErrorScreens;

        public BankIdWaitForScreenQueryBuilder waitForScreens(BankIdScreen... screens) {
            screensToWaitFor.addAll(asList(screens));
            return this;
        }

        public BankIdWaitForScreenQueryBuilder waitForScreens(List<BankIdScreen> screens) {
            screensToWaitFor.addAll(screens);
            return this;
        }

        public BankIdWaitForScreenQueryBuilder waitForSeconds(int seconds) {
            this.searchForSeconds = seconds;
            return this;
        }

        public BankIdWaitForScreenQueryBuilder verifyNoErrorScreens(boolean verifyNoErrorScreens) {
            this.verifyNoErrorScreens = verifyNoErrorScreens;
            return this;
        }

        public BankIdScreensQuery build() {
            return new BankIdScreensQuery(screensToWaitFor, searchForSeconds, verifyNoErrorScreens);
        }
    }
}
