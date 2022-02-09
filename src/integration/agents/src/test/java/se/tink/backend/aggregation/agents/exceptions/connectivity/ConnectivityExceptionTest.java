package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AccountInformationErrors.NO_ACCOUNTS;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.ACTION_NOT_PERMITTED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.ProviderErrors.PROVIDER_UNAVAILABLE;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.UNKNOWN_ERROR;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.STATIC_CREDENTIALS_INCORRECT;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.connectivity.errors.ConnectivityErrorType;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class ConnectivityExceptionTest {

    private static final RuntimeException TEST_CAUSE = new IllegalStateException("Testo testo");
    private static final String TEST_INTERNAL_MESSAGE = "Some internal message";
    private static final LocalizableKey TEST_USER_MESSAGE =
            new LocalizableKey("Authentication error.");

    @Test
    @Parameters(method = "parametersForShouldCreateDefaultConnectivityException")
    public void shouldCreateDefaultConnectivityException(
            ConnectivityException connectivityException,
            ConnectivityErrorType type,
            String reasonName,
            String displayMessage) {
        // expect
        assertThatThrownBy(() -> throwException(connectivityException))
                .hasMessage(String.format("Cause: %s.%s", type.name(), reasonName))
                .hasCause(null)
                .hasFieldOrPropertyWithValue(
                        "error",
                        ConnectivityError.newBuilder()
                                .setType(type)
                                .setDetails(
                                        ConnectivityErrorDetails.newBuilder()
                                                .setReason(reasonName)
                                                .build())
                                .setDisplayMessage(displayMessage)
                                .build())
                .hasFieldOrPropertyWithValue("userMessage", new LocalizableKey(displayMessage))
                .hasFieldOrPropertyWithValue("internalMessage", null);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldCreateDefaultConnectivityException() {
        return new Object[] {
            ExceptionParameters.builder()
                    .connectivityException(new ConnectivityException(NO_ACCOUNTS))
                    .type(ConnectivityErrorType.ACCOUNT_INFORMATION_ERROR)
                    .reasonName(NO_ACCOUNTS.name())
                    .displayMessage("Your bank account information is not available.")
                    .build()
                    .toDefaultExceptionParams(),
            ExceptionParameters.builder()
                    .connectivityException(new ConnectivityException(ACTION_NOT_PERMITTED))
                    .type(ConnectivityErrorType.AUTHORIZATION_ERROR)
                    .reasonName(ACTION_NOT_PERMITTED.name())
                    .displayMessage(
                            "You are not authorised to use this service. Please contact your bank.")
                    .build()
                    .toDefaultExceptionParams(),
            ExceptionParameters.builder()
                    .connectivityException(new ConnectivityException(PROVIDER_UNAVAILABLE))
                    .type(ConnectivityErrorType.PROVIDER_ERROR)
                    .reasonName(PROVIDER_UNAVAILABLE.name())
                    .displayMessage(
                            "A temporary problem has occurred with your bank. Please retry later.")
                    .build()
                    .toDefaultExceptionParams(),
            ExceptionParameters.builder()
                    .connectivityException(new ConnectivityException(UNKNOWN_ERROR))
                    .type(ConnectivityErrorType.TINK_SIDE_ERROR)
                    .reasonName(UNKNOWN_ERROR.name())
                    .displayMessage("A problem has occurred. Please retry later.")
                    .build()
                    .toDefaultExceptionParams(),
            ExceptionParameters.builder()
                    .connectivityException(new ConnectivityException(STATIC_CREDENTIALS_INCORRECT))
                    .type(ConnectivityErrorType.USER_LOGIN_ERROR)
                    .reasonName(STATIC_CREDENTIALS_INCORRECT.name())
                    .displayMessage(
                            "You have entered the wrong user name or/and password. Please try to log in again.")
                    .build()
                    .toDefaultExceptionParams(),
            ExceptionParameters.builder()
                    .connectivityException(new ConnectivityException(STATIC_CREDENTIALS_INCORRECT))
                    .type(ConnectivityErrorType.USER_LOGIN_ERROR)
                    .reasonName(STATIC_CREDENTIALS_INCORRECT.name())
                    .displayMessage(
                            "You have entered the wrong user name or/and password. Please try to log in again.")
                    .build()
                    .toDefaultExceptionParams(),
        };
    }

    @Test
    @Parameters(method = "parametersForShouldCreateConnectivityExceptionWithSpecificFields")
    public void shouldCreateConnectivityExceptionWithSpecificFields(
            ConnectivityException connectivityException,
            ConnectivityErrorType type,
            String reasonName) {
        // expect
        assertThatThrownBy(() -> throwException(connectivityException))
                .hasMessage(TEST_INTERNAL_MESSAGE)
                .hasCause(TEST_CAUSE)
                .hasFieldOrPropertyWithValue(
                        "error",
                        ConnectivityError.newBuilder()
                                .setType(type)
                                .setDetails(
                                        ConnectivityErrorDetails.newBuilder()
                                                .setReason(reasonName)
                                                .build())
                                .setDisplayMessage(TEST_USER_MESSAGE.get())
                                .build())
                .hasFieldOrPropertyWithValue("userMessage", TEST_USER_MESSAGE)
                .hasFieldOrPropertyWithValue("internalMessage", TEST_INTERNAL_MESSAGE);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldCreateConnectivityExceptionWithSpecificFields() {
        return new Object[] {
            ExceptionParameters.builder()
                    .connectivityException(
                            new ConnectivityException(NO_ACCOUNTS)
                                    .withCause(TEST_CAUSE)
                                    .withInternalMessage(TEST_INTERNAL_MESSAGE)
                                    .withUserMessage(TEST_USER_MESSAGE))
                    .type(ConnectivityErrorType.ACCOUNT_INFORMATION_ERROR)
                    .reasonName(NO_ACCOUNTS.name())
                    .build()
                    .toSpecificExceptionParams(),
            ExceptionParameters.builder()
                    .connectivityException(
                            new ConnectivityException(ACTION_NOT_PERMITTED)
                                    .withCause(TEST_CAUSE)
                                    .withInternalMessage(TEST_INTERNAL_MESSAGE)
                                    .withUserMessage(TEST_USER_MESSAGE))
                    .type(ConnectivityErrorType.AUTHORIZATION_ERROR)
                    .reasonName(ACTION_NOT_PERMITTED.name())
                    .build()
                    .toSpecificExceptionParams(),
            ExceptionParameters.builder()
                    .connectivityException(
                            new ConnectivityException(PROVIDER_UNAVAILABLE)
                                    .withCause(TEST_CAUSE)
                                    .withInternalMessage(TEST_INTERNAL_MESSAGE)
                                    .withUserMessage(TEST_USER_MESSAGE))
                    .type(ConnectivityErrorType.PROVIDER_ERROR)
                    .reasonName(PROVIDER_UNAVAILABLE.name())
                    .build()
                    .toSpecificExceptionParams(),
            ExceptionParameters.builder()
                    .connectivityException(
                            new ConnectivityException(UNKNOWN_ERROR)
                                    .withCause(TEST_CAUSE)
                                    .withInternalMessage(TEST_INTERNAL_MESSAGE)
                                    .withUserMessage(TEST_USER_MESSAGE))
                    .type(ConnectivityErrorType.TINK_SIDE_ERROR)
                    .reasonName(UNKNOWN_ERROR.name())
                    .build()
                    .toSpecificExceptionParams(),
            ExceptionParameters.builder()
                    .connectivityException(
                            new ConnectivityException(STATIC_CREDENTIALS_INCORRECT)
                                    .withCause(TEST_CAUSE)
                                    .withInternalMessage(TEST_INTERNAL_MESSAGE)
                                    .withUserMessage(TEST_USER_MESSAGE))
                    .type(ConnectivityErrorType.USER_LOGIN_ERROR)
                    .reasonName(STATIC_CREDENTIALS_INCORRECT.name())
                    .build()
                    .toSpecificExceptionParams(),
        };
    }

    private void throwException(ConnectivityException exception) {
        throw exception;
    }

    @Builder
    private static class ExceptionParameters {
        private final ConnectivityException connectivityException;
        private final ConnectivityErrorType type;
        private final String reasonName;
        private final String displayMessage;

        private Object[] toDefaultExceptionParams() {
            return new Object[] {
                this.connectivityException, this.type, this.reasonName, this.displayMessage
            };
        }

        private Object[] toSpecificExceptionParams() {
            return new Object[] {this.connectivityException, this.type, this.reasonName};
        }
    }
}
