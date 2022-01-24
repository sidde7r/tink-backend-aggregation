package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CHOOSE_METHOD_TITLE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CODE_APP_SCREEN_TITLE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CPR_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_ENTER_PASSWORD_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_ERROR_NOTIFICATION;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_USERNAME_INPUT;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator;

@Getter
@RequiredArgsConstructor
public enum MitIdScreen {
    USER_ID_SCREEN(LOC_USERNAME_INPUT),
    CODE_APP_SCREEN(LOC_CODE_APP_SCREEN_TITLE),
    ENTER_PASSWORD_SCREEN(LOC_ENTER_PASSWORD_INPUT),
    METHOD_SELECTOR_SCREEN(LOC_CHOOSE_METHOD_TITLE),
    CPR_SCREEN(LOC_CPR_INPUT),
    ERROR_NOTIFICATION_SCREEN(LOC_ERROR_NOTIFICATION);

    public static final ImmutableList<MitIdScreen> SECOND_FACTOR_SCREENS =
            ImmutableList.of(CODE_APP_SCREEN, ENTER_PASSWORD_SCREEN);

    public static Optional<MitIdScreen> getByMitIdLocator(MitIdLocator mitIdLocator) {
        return Stream.of(MitIdScreen.values())
                .filter(screen -> screen.getLocatorIdentifyingScreen() == mitIdLocator)
                .findFirst();
    }

    private final MitIdLocator locatorIdentifyingScreen;
}
