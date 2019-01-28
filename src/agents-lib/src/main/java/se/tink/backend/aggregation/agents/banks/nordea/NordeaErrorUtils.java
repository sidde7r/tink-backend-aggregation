package se.tink.backend.aggregation.agents.banks.nordea;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.core.signableoperation.SignableOperationStatuses;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public class NordeaErrorUtils {

    private static final Map<String, String> ERROR_MESSAGES = Maps.newHashMap();
    private static final Map<String, CredentialsStatus> ERROR_STATUSES = Maps.newHashMap();
    private static final Map<String, Exception> EXCEPTIONS = Maps.newHashMap();
    private static final Map<String, SignableOperationStatuses> TRANSFER_ERROR_STATUSES = Maps.newHashMap();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        /*
         * Blocked password
         * "Your personal code has been blocked. Please call Internet Support 0771-42 15 16 (from abroad
         * +46 771 42 15 16) to request a new code, or contact you local Nordea branch."
         */
        addErrorType(
                "MAS0002",
                "Din personliga kod är spärrad. Kontakta Nordeas kundtjänst (0771-42 15 16) för att beställa en ny " +
                        "kod, eller kontakta ditt lokala Nordeakontor.",
                CredentialsStatus.AUTHENTICATION_ERROR,
                AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.CODE_BLOCKED.getKey()));
        addErrorType(
                "MAS0010",
                "Din personliga kod är spärrad. Kontakta Nordeas kundtjänst (0771-42 15 16) för att beställa en ny " +
                        "kod, eller kontakta ditt lokala Nordeakontor.",
                CredentialsStatus.AUTHENTICATION_ERROR,
                AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.CODE_BLOCKED.getKey()));
        /*
         * Wrong password
         * "Personal identity number and code do not match. Please verify what you have entered and try again."
         */
        addErrorType(
                "MAS0004",
                "Felaktigt användarnamn eller lösenord.",
                CredentialsStatus.AUTHENTICATION_ERROR,
                LoginError.INCORRECT_CREDENTIALS.exception());
        /*
         * "Personal identity number and code do not match. Please verify what you have entered and try again."
         */
        addErrorType(
                "MAS0030",
                "Felaktigt användarnamn eller lösenord.",
                CredentialsStatus.AUTHENTICATION_ERROR,
                LoginError.INCORRECT_CREDENTIALS.exception());
        addErrorType(
                "MAS0031",
                "Felaktigt användarnamn eller lösenord.",
                CredentialsStatus.AUTHENTICATION_ERROR,
                LoginError.INCORRECT_CREDENTIALS.exception());
        /*
         * Unknown error (found occurrence--and frequently occurring--during LightLoginRequest)
         */
        addErrorType(
                "MAS9001",
                "Ett tillfälligt fel har uppstått.",
                CredentialsStatus.TEMPORARY_ERROR,
                new IllegalStateException("Unknown error (found occurrence--and frequently occurring--during LightLoginRequest)"));
        /*
         * Outdated client
         * "You are using an outdated version of the application. Please update your application in order to login"
         */
        addErrorType(
                "MAS9098",
                "Ett tillfälligt fel har uppstått.",
                CredentialsStatus.TEMPORARY_ERROR,
                new IllegalStateException("You are using an outdated version of the application. Please update your application in order to login"));
        /*
         * "Sorry, there is a technical error. Please log in again."
         */
        addErrorType(
                "MAS9099",
                "Tekniskt fel. Vänligen försök igen.",
                CredentialsStatus.TEMPORARY_ERROR,
                new IllegalStateException());
        /*
         * Bank ID authentication was cancelled
         * "Action cancelled"
         */
        addErrorType(
                "MBS0902",
                BankIdError.CANCELLED.userMessage().get(),
                CredentialsStatus.UNCHANGED,
                BankIdError.CANCELLED.exception());
        /*
         * Error during MobileBankIdInitialAuthentication
         * "Sorry, the login failed. This can be due to an incorrectly specified personal ID number, that the BankID
         * security app has not been started or that you are lacking a valid Mobile BankID."
         */
        addErrorType(
                "MBS0904",
                "Du saknar giltigt Mobilt BankID. Ladda ner BankID säkerhetsapp och logga in på Internetbanken för " +
                        "att beställa och ansluta Mobilt BankID.",
                CredentialsStatus.AUTHENTICATION_ERROR,
                BankIdError.NO_CLIENT.exception(UserMessage.NO_VALID_BANKID.getKey()));
        /*
         * If Bank ID authentication fails, and the user returns from the Bank ID app (invoking "bankid" to be sent to
         * CredentialsService.Supplement)
         * "No response from the mobile phone or tablet. Check that you have started up your BankID security app and a
         * working network connection. Follow the instructions in the mobile phone and try again."
         */
        addErrorType(
                "MBS0905",
                "Felaktiga Bank ID-uppgifter.",
                CredentialsStatus.UNCHANGED,
                BankIdError.NO_CLIENT.exception());
        /*
         * Bank ID authentication is requested when it's already in progress (i.e. when you cancel your Bank ID
         * authentication and then invokes it again)
         * "Confirmation for this personal identity number is already in process. Press cancel in the BankID security
         * app and try again."
         */
        addErrorType(
                "MBS0907",
                BankIdError.ALREADY_IN_PROGRESS.userMessage().get(),
                CredentialsStatus.UNCHANGED,
                BankIdError.ALREADY_IN_PROGRESS.exception());
        /*
         * "Sorry, an error has occurred. Please try again."
         */
        addErrorType(
                "MBS9099",
                "Ett tillfälligt fel har uppstått.",
                CredentialsStatus.TEMPORARY_ERROR,
                new IllegalStateException());

        /*
         * BankID is busy with the previous request. Please try again.
         */
        addErrorType(
                "MBS0913",
                BankIdError.ALREADY_IN_PROGRESS.userMessage().get(),
                CredentialsStatus.UNCHANGED,
                BankIdError.ALREADY_IN_PROGRESS.exception());
        /*
         * If not opening BankID app.
         */
        addErrorType(
                "MBS9001",
                BankIdError.NO_CLIENT.userMessage().get(),
                CredentialsStatus.UNCHANGED,
                BankIdError.NO_CLIENT.exception());
        /*
         * The user don't have a agreement with Nordea for internet and telephone banking.
         */
        addErrorType(
                "MBS0908",
                "Vi kan tyvärr inte hitta ett giltigt internetavtal, om du loggar in "
                + "hos Nordea med e-kod (kortläsaren) så kan du teckna ett avtal om internet- och telefonbank.",
                CredentialsStatus.AUTHENTICATION_ERROR,
                AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.NO_VALID_AGREEMENT.getKey()));

        /*
         * No results from custody account request
         * "Your custody accounts cannot be shown at the moment. Please try again later"
         */
        addErrorType(
                "MBS0110",
                "Dina placeringar kan inte visas just nu. Försök igen senare.",
                CredentialsStatus.TEMPORARY_ERROR,
                new IllegalStateException());
        
        /*
         * Transfer error codes with corresponding transfer statuses.
         */
        addTransferErrorMessage("MBS0502", "Täckning saknas på kontot.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0503", "Betalningsdag får vara högst fem år framåt i tiden.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0504", "Kontrollera meddelande.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0505", "Betalningsdag får inte vara tidigare än dagens datum", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0506", "Överföring finns redan.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0507", "Du har angett ett framtida betalningsdatum.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0508", "Du har angett ett ogiltigt betalningsdatum.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0509", "Beloppet måste anges i samma valuta som avsändarkontot när betalningsdagen ligger framåt i tiden.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0510", "Du har inte behörighet att göra betalningar och överföringar från valt konto.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0511", "Betalningsdatumet är inte en bankdag eller efter stängning.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0512", "Beloppet översteg den dagliga maxgränsen.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0513", "Kontot är spärrat.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0514", "Valuta på från-konto är inte samma som på till-konto.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0515", "Från-konto kan inte vara samma om till-konto.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0521", "Ett fel uppstod. Vänligen kontrollera Obekräftade betalningar.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0522", "Ett fel uppstod. Kontrollera statusen för dina betalningar under Obekräftade betalningar, Betalningar som förfaller, och Kontotransaktioner.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0523", "Otillåtet belopp vad avser MIN/MAX-gränser", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0524", "Ange mottagarkonto.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0525", "Kontrollera mottagarens kontonummer.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0526", "Kontrollera mottagarens namn.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0527", "Ange referensnummer.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0528", "Kontrollera OCR-numret.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0529", "A reference number is not allowed.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0530", "Skriv meddelande.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0531", "Kontrollera meddelandet.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0532", "Meddelande inte tillåtet.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0533", "Ange betalningens förfallodag.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0534", "Kontrollera betalningsdagen.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0535", "Ändra betalningsdagen till morgondagens datum eller senare.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0536", "Ange belopp.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0537", "Kontrollera beloppet. Ange beloppet i följande format 1234,00.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0538", "Kontrollera antalet betalningar.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0539", "Kontrollera betalningsfrekvensen.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0540", "Kontrollera betalningsdagen för den sista betalningen.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0541", "Betalningen behandlas just nu.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0542", "Betalningen är avslutad.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0543", "Betalningen kan inte ändras.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0544", "Uppgifterna om betalningen kan inte ändras.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0545", "Betalningen har ändrats. Hämta betalningen på nytt.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0546", "Beloppet överstiger den dagliga maxgränsen.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0547", "Kontrollera mottagarens namn.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0549", "Ange OCR eller meddelande.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0550", "Du kan inte spara samma mottagare två gånger.", SignableOperationStatuses.CANCELLED);
        addTransferErrorMessage("MBS0551", "Betalningar med OCR-nummer kan enbart göras i SEK.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0552", "Du kan inte spara eller signera två exakt likadana betalningar eller överföringar.", SignableOperationStatuses.FAILED);
        addTransferErrorMessage("MBS0553", "För att använda denna tjänst behöver du ha minst två giltiga konton att flytta pengar emellan.", SignableOperationStatuses.FAILED);
    }

    private static void addErrorType(String type, String message, CredentialsStatus status, Exception exception) {
        ERROR_MESSAGES.put(type, message);
        ERROR_STATUSES.put(type, status);
        EXCEPTIONS.put(type, exception);
    }

    public static String getErrorMessage(String code) {
        return getErrorMessage(code, "Ett okänt fel har uppstått");
    }

    public static String getErrorMessage(String code, String defaultMessage) {
        String message = ERROR_MESSAGES.get(code);

        if (message == null) {
            message = defaultMessage;
        }

        return message;
    }

    public static CredentialsStatus getErrorStatus(String code) {
        CredentialsStatus status = ERROR_STATUSES.get(code);

        if (status == null) {
            status = CredentialsStatus.TEMPORARY_ERROR;
        }

        return status;
    }

    public static void throwError(String code) throws AuthenticationException, AuthorizationException {
        Exception exception = EXCEPTIONS.get(code);
        Preconditions.checkNotNull(code, "NordeaV20 failed with unknown error code %s", code);

        if (exception instanceof AuthenticationException) {
            throw (AuthenticationException) exception;
        } else if (exception instanceof AuthorizationException) {
            throw (AuthorizationException) exception;
        }

        throw new IllegalStateException(exception);
    }

    private static void addTransferErrorMessage(String type, String message, SignableOperationStatuses status) {
        ERROR_MESSAGES.put(type, message);
        TRANSFER_ERROR_STATUSES.put(type, status);
    }

    public static SignableOperationStatuses getTransferErrorStatus(String code) {
        SignableOperationStatuses status = TRANSFER_ERROR_STATUSES.get(code);
        return status == null ? SignableOperationStatuses.FAILED : status;
    }

    private enum UserMessage implements LocalizableEnum {
        CODE_BLOCKED(new LocalizableKey("Your personal code has been locked. Contact Nordea customer services (0771-42 15 16) to order a new code, or contact your local Nordea office.")),
        NO_VALID_BANKID(new LocalizableKey("You're missing a valid Mobilt BankID. Download the BankID app and login to Internetbanken to order and connect to Mobil BankID.")),
        NO_VALID_AGREEMENT(new LocalizableKey("We could not find a valid internet banking agreement. If you login to Nordea's internetbank with e-code (card reader) you may sign an agreement for internet and telephone banking"));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }
        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }
}
