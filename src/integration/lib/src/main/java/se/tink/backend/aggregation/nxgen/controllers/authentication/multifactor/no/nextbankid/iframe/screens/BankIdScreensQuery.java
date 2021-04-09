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
    private final int searchForSeconds;

    public static BankIdWaitForScreenQueryBuilder builder() {
        return new BankIdWaitForScreenQueryBuilder();
    }

    public static class BankIdWaitForScreenQueryBuilder {

        private final List<BankIdScreen> screensToWaitFor = new ArrayList<>();
        private int searchForSeconds = BankIdConstants.DEFAULT_WAIT_FOR_ELEMENT_TIMEOUT_IN_SECONDS;

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

        public BankIdScreensQuery build() {
            return new BankIdScreensQuery(screensToWaitFor, searchForSeconds);
        }
    }
}
