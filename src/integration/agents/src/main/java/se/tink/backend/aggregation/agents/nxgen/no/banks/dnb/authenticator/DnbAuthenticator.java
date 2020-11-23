package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator;

import java.net.URI;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.rpc.CollectChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.rpc.InitiateBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.rpc.InstrumentInfoResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticatorNO;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.i18n.LocalizableKey;

public class DnbAuthenticator implements BankIdAuthenticatorNO {
    private final DnbApiClient apiClient;
    private URI bankIdReferer;
    private static final String LOCATION = "Location";
    private static final Pattern BANKID_ERROR_PATTERN = Pattern.compile("feilkode c.{3}");

    public DnbAuthenticator(DnbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String init(String nationalId, String dob, String mobilenumber)
            throws BankIdException, LoginException {
        // We need this to fetch the cookies before doing the post request
        apiClient.getStartMobile();

        // From this request we'll get a location that contains the encrypted uid
        HttpResponse startMobileResponse = apiClient.postStartMobile(nationalId);

        bankIdReferer = extractLocationFromStartMobileResponse(startMobileResponse);
        verifyStartMobileResponse(startMobileResponse);

        apiClient.initiateSession(bankIdReferer);

        InstrumentInfoResponse instrumentInfoResponse = apiClient.getInstrumentInfo(bankIdReferer);
        verifyInstrumentInfoResponse(instrumentInfoResponse);

        InitiateBankIdResponse initiateBankIdResponse = apiClient.getInitiateBankId(bankIdReferer);
        verifyInitiateBankIdResponse(initiateBankIdResponse);

        CollectChallengeResponse collectChallengeResponse =
                apiClient.postCollectChallenge(bankIdReferer, mobilenumber);

        if (!collectChallengeResponse.isSuccess()) {
            String userMessage =
                    collectChallengeResponse.getMessage().getUserMessage().toLowerCase();
            verifyCollectChallengeResponse(userMessage);
            String errorCode = getBankIdErrorCode(collectChallengeResponse, userMessage);
            handleBankIdError(collectChallengeResponse, errorCode);
        }

        return collectChallengeResponse.getMessage().getApplicationData();
    }

    private URI extractLocationFromStartMobileResponse(HttpResponse startMobileResponse) {
        // Sometimes the Location header looks like:
        // Location: https://m.dnb.no, m.dnb.no/segp/appo/logon/main..
        // Which just doesn't compute
        String location = startMobileResponse.getHeaders().getFirst(LOCATION);
        if (StringUtils.isEmpty(location)) {
            return null;
        }
        return URI.create(location.replace("m.dnb.no, ", ""));
    }

    private void handleBankIdError(
            CollectChallengeResponse collectChallengeResponse, String errorCode)
            throws BankIdException, LoginException {
        switch (errorCode) {
            case DnbConstants.Messages.BANKID_ALREADY_IN_PROGRESS:
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            case DnbConstants.Messages.INCORRECT_PHONE_NUMER_OR_INACTIVATED_MOBILE_BANKID:
                throw LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE.exception(
                        new LocalizableKey(
                                "Error Code: C161. "
                                        + LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE
                                                .userMessage()
                                                .get()));
            case DnbConstants.Messages.BANKID_BLOCKED_A:
                throw BankIdError.BLOCKED.exception(
                        new LocalizableKey(
                                "Error Code C176. " + BankIdError.BLOCKED.userMessage().get()));
            case DnbConstants.Messages.BANKID_BLOCKED_B:
            case DnbConstants.Messages.BANKID_BLOCKED_C:
                throw BankIdError.BLOCKED.exception(
                        new LocalizableKey(
                                "Error Code C30F. " + BankIdError.BLOCKED.userMessage().get()));
            case DnbConstants.Messages.BANKID_BLOCKED_D:
                throw BankIdError.BLOCKED.exception(
                        new LocalizableKey(
                                "Error Code C307. Your BankID is blocked due to incorrect PIN"
                                        + "Log in to the internet bank in another way and reset BankID on mobile."));
            case DnbConstants.Messages.ERROR_MOBILE_OPERATOR_A:
            case DnbConstants.Messages.ERROR_MOBILE_OPERATOR_B:
            case DnbConstants.Messages.ERROR_MOBILE_OPERATOR_C:
                throw LoginError.ERROR_WITH_MOBILE_OPERATOR.exception(
                        new LocalizableKey(
                                "Error Code C202. "
                                        + LoginError.ERROR_WITH_MOBILE_OPERATOR
                                                .userMessage()
                                                .get()));
            case DnbConstants.Messages.ERROR_MOBILE_OPERATOR_D:
            case DnbConstants.Messages.ERROR_MOBILE_OPERATOR_E:
                throw LoginError.ERROR_WITH_MOBILE_OPERATOR.exception(
                        new LocalizableKey(
                                "Error Code C131. This error indicates that your mobile operator has trouble or a process is"
                                        + " running on your phone number. Restart your phone and try again in 5 minutes."));
            case DnbConstants.Messages.ERROR_MOBILE_OPERATOR_F:
                throw LoginError.ERROR_WITH_MOBILE_OPERATOR.exception(
                        new LocalizableKey(
                                "Error code C308. There was a timeout due to slow response time. This can happen if there"
                                        + " are weak or unstable signals or if the mobile operator is having trouble."
                                        + " Please try again in 5 minutes."));
            case DnbConstants.Messages.ERROR_MOBILE_OPERATOR_G:
                throw LoginError.ERROR_WITH_MOBILE_OPERATOR.exception(
                        new LocalizableKey(
                                "Error code C302. This error occurs when you have switched mobile networks, for example,"
                                        + " if you have just been abroad. Restart your phone and try again."));
            default:
                throw LoginError.DEFAULT_MESSAGE.exception(
                        String.format(
                                "could not initiate bankid, user message: %s, error message: %s",
                                collectChallengeResponse.getMessage().getUserMessage(),
                                collectChallengeResponse.getMessage().getErrorMessage()));
        }
    }

    private String getBankIdErrorCode(
            CollectChallengeResponse collectChallengeResponse, String userMessage) {
        String errorCode;
        Matcher matcher = BANKID_ERROR_PATTERN.matcher(userMessage);
        if (!matcher.find()) {
            throw LoginError.DEFAULT_MESSAGE.exception(
                    String.format(
                            "could not initiate bankid, user message: %s, error message: %s",
                            collectChallengeResponse.getMessage().getUserMessage(),
                            collectChallengeResponse.getMessage().getErrorMessage()));
        } else {
            errorCode = matcher.group(0);
        }
        return errorCode;
    }

    private void verifyCollectChallengeResponse(String userMessage) throws BankIdException {
        if (Objects.equals(DnbConstants.Messages.GENERIC_BANKID_ERROR, userMessage)) {
            throw BankIdError.BLOCKED.exception(
                    new LocalizableKey(
                            "Have you received a new mobile phone, made changes to your mobile subscription "
                                    + "or have a new SIM card? In that case, you must delete BankID Mobile and re-enable the service."));
        }
    }

    private void verifyInitiateBankIdResponse(InitiateBankIdResponse initiateBankIdResponse) {
        if (!initiateBankIdResponse.isSuccess()) {
            if (initiateBankIdResponse.getStatus().isUserError()
                    && DnbConstants.Messages.USER_ID_BLOCKED.equalsIgnoreCase(
                            initiateBankIdResponse.getMessage().getErrorMessage())) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception();
            }
            throw LoginError.DEFAULT_MESSAGE.exception(
                    String.format(
                            "error msg: %s, user msg: %s",
                            initiateBankIdResponse.getMessage().getErrorMessage(),
                            initiateBankIdResponse.getMessage().getUserMessage()));
        }
    }

    private void verifyInstrumentInfoResponse(InstrumentInfoResponse instrumentInfoResponse) {
        if (!instrumentInfoResponse.isSuccess()) {
            throw LoginError.DEFAULT_MESSAGE.exception("Could not fetch instrument info");
        }
    }

    private void verifyStartMobileResponse(HttpResponse startMobileResponse)
            throws BankIdException {
        if (bankIdReferer == null) {
            if (startMobileResponse.hasBody()
                    && startMobileResponse
                            .getBody(String.class)
                            .toLowerCase()
                            .contains(DnbConstants.Messages.SSN_FORMAT_ERROR)) {
                throw BankIdError.USER_VALIDATION_ERROR.exception();
            }

            throw LoginError.DEFAULT_MESSAGE.exception();
        }
    }

    @Override
    public BankIdStatus collect() throws AuthenticationException, AuthorizationException {
        CollectChallengeResponse collectBankId = apiClient.getCollectBankId(bankIdReferer);

        if (collectBankId.isSuccess()) {
            apiClient.getFinalizeLogon(bankIdReferer);
            apiClient.getFirstRequestAfterLogon(bankIdReferer);
            return BankIdStatus.DONE;
        }

        if (collectBankId.isRetry()) {
            return BankIdStatus.WAITING;
        }

        if (isTimeout(collectBankId)) {
            return BankIdStatus.TIMEOUT;
        }

        if (Objects.equals(
                DnbConstants.Messages.GENERIC_BANKID_ERROR,
                collectBankId.getMessage().getUserMessage().toLowerCase())) {
            return BankIdStatus.FAILED_UNKNOWN;
        }

        throw LoginError.DEFAULT_MESSAGE.exception(
                String.format(
                        "user message: %s, error message: %s",
                        collectBankId.getMessage().getUserMessage(),
                        collectBankId.getMessage().getErrorMessage()));
    }

    private boolean isTimeout(CollectChallengeResponse collectBankId) {
        String userMessage = collectBankId.getMessage().getUserMessage().toLowerCase();
        return userMessage.contains(DnbConstants.Messages.BANKID_TIMEOUT)
                || userMessage.contains(DnbConstants.Messages.LOGIN_TIMEOUT);
    }
}
