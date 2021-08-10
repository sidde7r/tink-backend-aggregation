package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe;

import static java.util.stream.Collectors.toList;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.MOBILE_BANK_ID_ENTER_MOBILE_NUMBER_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.MOBILE_BANK_ID_SEND_REQUEST_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.ONE_TIME_CODE_METHOD_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.THIRD_PARTY_APP_METHOD_SCREEN;

import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;

@Getter
public enum BankId2FAMethod {
    MOBILE_BANK_ID_METHOD(
            MOBILE_BANK_ID_SEND_REQUEST_SCREEN, MOBILE_BANK_ID_ENTER_MOBILE_NUMBER_SCREEN),
    ONE_TIME_CODE_METHOD(ONE_TIME_CODE_METHOD_SCREEN),
    THIRD_PARTY_APP_METHOD(THIRD_PARTY_APP_METHOD_SCREEN);

    private final List<BankIdScreen> screensDedicatedForMethod;

    BankId2FAMethod(BankIdScreen... screens) {
        this.screensDedicatedForMethod = Stream.of(screens).collect(toList());
    }

    public static BankId2FAMethod get2FAMethodByScreen(BankIdScreen screen) {
        return Stream.of(BankId2FAMethod.values())
                .filter(method -> method.getScreensDedicatedForMethod().contains(screen))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Cannot find 2FA method for screen: " + screen));
    }
}
