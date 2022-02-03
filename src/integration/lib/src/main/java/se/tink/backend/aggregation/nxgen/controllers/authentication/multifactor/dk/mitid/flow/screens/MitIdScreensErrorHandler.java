package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_ERROR_NOTIFICATION;

import com.google.inject.Inject;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdError;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocators;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MitIdScreensErrorHandler {

    private final WebDriverService driverService;
    private final MitIdLocators locators;

    private static final String CANNOT_FIND_SCREEN_EXCEPTION_FORMAT =
            "\nCould not find expected screens"
                    + "\nExpected screens: %s"
                    + "\nCurrent screen: [%s]";

    private static final String ERROR_SCREEN_EXCEPTION_FORMAT =
            "\nUnexpected error screen found" + "\nError message: [%s]" + "\nExpected screens: %s";

    public RuntimeException cannotFindScreenException(
            MitIdScreenQuery screenQuery, @Nullable MitIdScreen currentScreen) {
        String exceptionMessage =
                String.format(
                        CANNOT_FIND_SCREEN_EXCEPTION_FORMAT,
                        screenQuery.getExpectedScreensToSearchFor(),
                        currentScreen);
        return new IllegalStateException(exceptionMessage);
    }

    public RuntimeException unexpectedErrorScreenException(MitIdScreenQuery screenQuery) {
        String errorMessage =
                readErrorMessage()
                        .map(MitIdScreensErrorHandler::removeNewLinesForClearKibanaMessage)
                        .orElse(null);
        if (errorMessage == null) {
            return unexpectedErrorScreenException(
                    MitIdError.CANNOT_FIND_ERROR_NOTIFICATION, "", screenQuery);
        }

        MitIdError error =
                MitIdConstants.Errors.findErrorForMessage(errorMessage)
                        .orElse(MitIdError.UNKNOWN_ERROR_NOTIFICATION);
        return unexpectedErrorScreenException(error, errorMessage, screenQuery);
    }

    private Optional<String> readErrorMessage() {
        return driverService
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(locators.getElementLocator(LOC_ERROR_NOTIFICATION))
                                .searchOnlyOnce()
                                .build())
                .getFirstFoundElement()
                .map(element -> element.getAttribute("textContent"));
    }

    private static String removeNewLinesForClearKibanaMessage(String message) {
        return Stream.of(message.split("\\n"))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" /n "));
    }

    private static MitIdException unexpectedErrorScreenException(
            MitIdError error, String errorMessage, MitIdScreenQuery screenQuery) {
        String internalMessage =
                String.format(
                        ERROR_SCREEN_EXCEPTION_FORMAT,
                        errorMessage,
                        screenQuery.getExpectedScreensToSearchFor());
        return error.exception(internalMessage);
    }
}
