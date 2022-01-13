package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurSessionState;
import se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators.DefaultLoginValidatorFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators.LoginValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators.LoginValidatorEmpty;
import se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators.LoginValidatorErrorMessageFinder;
import se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators.LoginValidatorSuccessMessageFinder;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.connectivity.errors.ConnectivityErrorDetails;

@AllArgsConstructor
public class CajasurLoginValidatorFactory extends DefaultLoginValidatorFactory<String> {

    private static final String FORM_ERROR_DISCRIMINATOR = "name=\"frmError\"";
    private SessionStorage sessionStorage;

    @Override
    public LoginValidator createChangePasswordErrorValidator() {
        return LoginValidatorEmpty.getInstance();
    }

    @Override
    public LoginValidator createAccountTypeErrorValidator() {
        return LoginValidatorEmpty.getInstance();
    }

    @Override
    public LoginValidator createBankServiceErrorValidator() {
        return LoginValidatorEmpty.getInstance();
    }

    @Override
    public LoginValidator createIncorrectCredentialsErrorValidator() {
        ConnectivityException incorrectCredentialsError =
                new ConnectivityException(
                        ConnectivityErrorDetails.UserLoginErrors.STATIC_CREDENTIALS_INCORRECT);
        return new LoginValidatorErrorMessageFinder(
                        incorrectCredentialsError,
                        FORM_ERROR_DISCRIMINATOR,
                        "o la clave de acceso introducida es err")
                .addMessagesExceptionPair(
                        incorrectCredentialsError,
                        FORM_ERROR_DISCRIMINATOR,
                        "lguno de los datos introducidos no es correcto")
                .addMessagesExceptionPair(incorrectCredentialsError, "nº de tarjeta:")
                .addMessagesExceptionPair(
                        incorrectCredentialsError,
                        FORM_ERROR_DISCRIMINATOR,
                        "Comprueba que has introducido correctamente tus datos")
                .addMessagesExceptionPair(
                        incorrectCredentialsError,
                        FORM_ERROR_DISCRIMINATOR,
                        "El NIF/CIF introducido no es correcto")
                .addMessagesExceptionPair(
                        incorrectCredentialsError,
                        FORM_ERROR_DISCRIMINATOR,
                        "DATOS INCORRECTOS, VUELVA A INTENTARLO")
                .addMessagesExceptionPair(
                        incorrectCredentialsError, "login/errorLogin.jsp", "form name=\"frmError\"")
                .addMessagesExceptionPair(
                        incorrectCredentialsError,
                        FORM_ERROR_DISCRIMINATOR,
                        "El NIF/NIE o la tarjeta introducidos no son correctos.");
    }

    @Override
    public LoginValidator createNotActivatedErrorValidator() {
        ConnectivityException notActivatedUserError =
                new ConnectivityException(
                        ConnectivityErrorDetails.AuthorizationErrors
                                .USER_ACTION_REQUIRED_UNSIGNED_AGREEMENT);
        return new LoginValidatorErrorMessageFinder(
                        notActivatedUserError,
                        FORM_ERROR_DISCRIMINATOR,
                        "La tarjeta utilizada para conectarte ha sido renovada recientemente")
                .addMessagesExceptionPair(
                        notActivatedUserError,
                        FORM_ERROR_DISCRIMINATOR,
                        "para que Ud. pueda seguir operando")
                .addMessagesExceptionPair(
                        notActivatedUserError,
                        FORM_ERROR_DISCRIMINATOR,
                        "Ahora te pedimos que sigas unos sencillos pasos")
                .addMessagesExceptionPair(
                        notActivatedUserError,
                        FORM_ERROR_DISCRIMINATOR,
                        "para completar el proceso de configuraci");
    }

    @Override
    public LoginValidator createPendingConfirmationErrorValidator() {
        ConnectivityException notActivatedUserError =
                new ConnectivityException(
                        ConnectivityErrorDetails.AuthorizationErrors
                                .USER_ACTION_REQUIRED_UNSIGNED_AGREEMENT);
        return new LoginValidatorErrorMessageFinder(
                        notActivatedUserError,
                        FORM_ERROR_DISCRIMINATOR,
                        "Te recordamos que tienes todav",
                        "de la Tarjeta Personal de Claves")
                .addMessagesExceptionPair(
                        notActivatedUserError,
                        FORM_ERROR_DISCRIMINATOR,
                        "Por seguridad algunos accesos requieren una autorización adicional.");
    }

    @Override
    public LoginValidator createScaErrorValidator() {
        ConnectivityException dynamicCredentialsWrongError =
                new ConnectivityException(
                        ConnectivityErrorDetails.UserLoginErrors.DYNAMIC_CREDENTIALS_INCORRECT);

        return new LoginValidatorScaError(
                        sessionStorage,
                        dynamicCredentialsWrongError,
                        FORM_ERROR_DISCRIMINATOR,
                        "Te recordamos que tienes todav",
                        "de la Tarjeta Personal de Claves")
                .addMessagesExceptionPair(
                        dynamicCredentialsWrongError,
                        FORM_ERROR_DISCRIMINATOR,
                        "Por seguridad algunos accesos requieren una autorización adicional.");
    }

    @Override
    public LoginValidator createUserBlockedErrorValidator() {
        ConnectivityException blockedUserError =
                new ConnectivityException(ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED);
        return new LoginValidatorErrorMessageFinder(
                        blockedUserError,
                        FORM_ERROR_DISCRIMINATOR,
                        "porque esta tarjeta ha caducado")
                .addMessagesExceptionPair(blockedUserError, "Ya no puedes operar con esta tarjeta")
                .addMessagesExceptionPair(
                        blockedUserError,
                        FORM_ERROR_DISCRIMINATOR,
                        "e ha bloqueado tu acceso a Banca Online")
                .addMessagesExceptionPair(
                        blockedUserError,
                        FORM_ERROR_DISCRIMINATOR,
                        "Tu Tarjeta Personal de Claves est",
                        "bloqueada y");
    }

    @Override
    public LoginValidator createLoginSuccessValidator() {
        return new LoginValidatorSuccessMessageFinder(
                new ConnectivityException(
                        ConnectivityErrorDetails.AuthorizationErrors.UNRECOGNIZED),
                "name=\"frmLogin\"");
    }

    private static class LoginValidatorScaError extends LoginValidatorErrorMessageFinder {

        private final SessionStorage sessionStorage;

        public LoginValidatorScaError(
                SessionStorage sessionStorage,
                ConnectivityException exceptionToThrow,
                String... messagesForFinding) {
            super(exceptionToThrow, messagesForFinding);
            this.sessionStorage = sessionStorage;
        }

        @Override
        public void validate(String body) {
            if (CajasurSessionState.getInstance(sessionStorage).isScaPerformed()) {
                super.validate(body);
            }
        }
    }
}
