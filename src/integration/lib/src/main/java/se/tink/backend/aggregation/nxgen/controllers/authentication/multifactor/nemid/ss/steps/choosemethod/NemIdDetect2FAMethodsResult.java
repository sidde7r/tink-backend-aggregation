package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod;

import static java.util.Collections.singleton;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdDetect2FAMethodsResult.ResultType.CAN_CHOOSE_METHOD_FROM_POPUP;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdDetect2FAMethodsResult.ResultType.CAN_ONLY_USE_DEFAULT_METHOD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdDetect2FAMethodsResult.ResultType.CAN_TOGGLE_BETWEEN_2_METHODS;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethodScreen;

/**
 * This class describes which 2FA methods were found to be available and on which {@link
 * NemId2FAMethodScreen} we ended up at the end of detection process.
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class NemIdDetect2FAMethodsResult {

    private final ResultType resultType;

    private final NemId2FAMethodScreen currentScreen;
    private final Set<NemId2FAMethod> availableMethods;

    public static NemIdDetect2FAMethodsResult canOnlyUseDefaultMethod(
            NemId2FAMethodScreen defaultScreen) {

        NemId2FAMethod defaultMethod = defaultScreen.getSupportedMethod();
        return NemIdDetect2FAMethodsResult.builder()
                .resultType(CAN_ONLY_USE_DEFAULT_METHOD)
                .currentScreen(defaultScreen)
                .availableMethods(singleton(defaultMethod))
                .build();
    }

    public static NemIdDetect2FAMethodsResult canChooseMethodFromPopup(
            NemId2FAMethodScreen defaultScreen, Set<NemId2FAMethod> methodsInPopup) {

        NemId2FAMethod defaultMethod = defaultScreen.getSupportedMethod();
        return NemIdDetect2FAMethodsResult.builder()
                .resultType(CAN_CHOOSE_METHOD_FROM_POPUP)
                .currentScreen(defaultScreen)
                .availableMethods(
                        ImmutableSet.<NemId2FAMethod>builder()
                                .add(defaultMethod)
                                .addAll(methodsInPopup)
                                .build())
                .build();
    }

    public static NemIdDetect2FAMethodsResult canToggleBetween2Methods(
            NemId2FAMethodScreen defaultScreen, NemId2FAMethodScreen secondMethodScreen) {

        NemId2FAMethod defaultMethod = defaultScreen.getSupportedMethod();
        NemId2FAMethod secondMethod = secondMethodScreen.getSupportedMethod();
        return NemIdDetect2FAMethodsResult.builder()
                .resultType(CAN_TOGGLE_BETWEEN_2_METHODS)
                .currentScreen(secondMethodScreen)
                .availableMethods(ImmutableSet.of(defaultMethod, secondMethod))
                .build();
    }

    public enum ResultType {
        CAN_ONLY_USE_DEFAULT_METHOD,
        CAN_CHOOSE_METHOD_FROM_POPUP,
        CAN_TOGGLE_BETWEEN_2_METHODS
    }
}
