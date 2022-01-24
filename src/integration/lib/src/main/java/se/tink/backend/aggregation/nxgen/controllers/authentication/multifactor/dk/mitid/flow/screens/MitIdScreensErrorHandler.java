package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens;

import static java.lang.String.format;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_ERROR_NOTIFICATION;

import com.google.inject.Inject;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdError;
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
            MitIdScreensQuery screensQuery, @Nullable MitIdScreen currentScreen) {
        String exceptionMessage =
                String.format(
                        CANNOT_FIND_SCREEN_EXCEPTION_FORMAT,
                        screensQuery.getExpectedScreensToSearchFor(),
                        currentScreen);
        return new IllegalStateException(exceptionMessage);
    }

    public RuntimeException unexpectedErrorScreenException(MitIdScreensQuery screensQuery) {
        String errorMessage = readErrorMessage().orElse(null);
        if (errorMessage != null) {
            // remove new lines to consistently have only 1 line in Kibana
            errorMessage =
                    Stream.of(errorMessage.split("\\n"))
                            .map(String::trim)
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.joining(" /n "));
        }

        String exceptionMessage =
                format(
                        ERROR_SCREEN_EXCEPTION_FORMAT,
                        errorMessage,
                        screensQuery.getExpectedScreensToSearchFor());

        MitIdError error =
                MitIdConstants.Errors.findErrorForMessage(errorMessage)
                        .orElse(MitIdError.UNKNOWN_ERROR_NOTIFICATION);
        return error.exception(exceptionMessage);
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
}
