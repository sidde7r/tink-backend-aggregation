package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.BANK_ID_APP_METHOD_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.CODE_CHIP_METHOD_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.MOBILE_BANK_ID_METHOD_SCREEN;

import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;

@Getter
@RequiredArgsConstructor
public enum BankId2FAMethod {
    MOBILE_BANK_ID_METHOD(MOBILE_BANK_ID_METHOD_SCREEN),
    CODE_CHIP_METHOD(CODE_CHIP_METHOD_SCREEN),
    BANK_ID_APP_METHOD(BANK_ID_APP_METHOD_SCREEN);

    private final BankIdScreen screenDedicatedForMethod;

    public static BankId2FAMethod get2FAMethodByScreen(BankIdScreen screen) {
        return Stream.of(BankId2FAMethod.values())
                .filter(method -> method.getScreenDedicatedForMethod() == screen)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Cannot find 2FA method for screen: " + screen));
    }
}
