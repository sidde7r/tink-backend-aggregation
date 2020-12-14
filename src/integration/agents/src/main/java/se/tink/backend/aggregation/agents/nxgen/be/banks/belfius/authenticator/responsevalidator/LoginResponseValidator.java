package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ScreenUpdateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.Widget;

public class LoginResponseValidator {

    private static final String WIDGET_ID_REGISTRATION_INA_DESCR =
            "Container@reuse_LoginPw@mlb_dev_registration_ina_descr";
    private static final String WIDGET_ID_REGISTRATION_FLAG_INACTIVE =
            "Container@reuse_LoginPw@mlb_dev_registration_flag_inactive";

    public static LoginResponseStatus validate(LoginResponse loginResponse) {
        final LoginResponseStatus loginResponseStatus =
                checkIfResponseContainsLoginErrors(loginResponse);

        return (loginResponseStatus == LoginResponseStatus.NO_ERRORS)
                ? checkIfSCAIsNecessary(loginResponse)
                : loginResponseStatus;
    }

    private static LoginResponseStatus checkIfSCAIsNecessary(LoginResponse loginResponse) {
        final Map<String, List<Widget>> widgetsById = getWidgetsById(loginResponse);

        return (isRegistrationFlagInactivePresent(widgetsById) || isInaDescrPresent(widgetsById))
                ? LoginResponseStatus.SESSION_EXPIRED
                : LoginResponseStatus.NO_ERRORS;
    }

    private static Map<String, List<Widget>> getWidgetsById(LoginResponse loginResponse) {
        return loginResponse
                .filter(ScreenUpdateResponse.class)
                .map(ScreenUpdateResponse::getWidgets)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Widget::getWidgetId));
    }

    private static boolean isInaDescrPresent(Map<String, List<Widget>> widgetsById) {
        return isTextPropertyPresent(
                widgetsById, WIDGET_ID_REGISTRATION_INA_DESCR, "opnieuw registreren");
    }

    private static boolean isRegistrationFlagInactivePresent(
            Map<String, List<Widget>> widgetsById) {

        return isTextPropertyPresent(widgetsById, WIDGET_ID_REGISTRATION_FLAG_INACTIVE, "Y");
    }

    private static boolean isTextPropertyPresent(
            Map<String, List<Widget>> widgetsById,
            String seekedWidgetId,
            String expectedStringContained) {

        return CollectionUtils.emptyIfNull(widgetsById.get(seekedWidgetId)).stream()
                .map(Widget::getTextProperty)
                .filter(StringUtils::isNotBlank)
                .anyMatch(s -> s.contains(expectedStringContained));
    }

    private static LoginResponseStatus checkIfResponseContainsLoginErrors(LoginResponse response) {
        final MessageResponse messageResponse =
                response.filter(MessageResponse.class).findFirst().orElse(null);

        return Objects.nonNull(messageResponse)
                ? checkMessageResponseForErrors(messageResponse)
                : LoginResponseStatus.NO_ERRORS;
    }

    private static LoginResponseStatus checkMessageResponseForErrors(
            MessageResponse messageResponse) {
        if (!messageResponse
                        .getMessageType()
                        .equalsIgnoreCase(BelfiusConstants.ErrorCodes.ERROR_MESSAGE_TYPE)
                && !messageResponse
                        .getMessageType()
                        .equalsIgnoreCase(ErrorCodes.FATAL_MESSAGE_TYPE)) {
            return LoginResponseStatus.NO_ERRORS;
        }

        if (StringUtils.containsIgnoreCase(
                messageResponse.getMessageDetail(),
                BelfiusConstants.ErrorCodes.WRONG_CREDENTIALS_CODE)) {
            return LoginResponseStatus.INCORRECT_CREDENTIALS;
        } else if (StringUtils.containsIgnoreCase(
                messageResponse.getMessageDetail(), BelfiusConstants.ErrorCodes.ACCOUNT_BLOCKED)) {
            return LoginResponseStatus.ACCOUNT_BLOCKED;
        } else if (StringUtils.containsIgnoreCase(
                        messageResponse.getMessageContent(), ErrorCodes.UNKNOWN_SESSION)
                || StringUtils.containsIgnoreCase(
                        messageResponse.getMessageContent(), ErrorCodes.SESSION_EXPIRED)) {
            return LoginResponseStatus.SESSION_EXPIRED;
        } else {
            return LoginResponseStatus.NO_ERRORS;
        }
    }
}
