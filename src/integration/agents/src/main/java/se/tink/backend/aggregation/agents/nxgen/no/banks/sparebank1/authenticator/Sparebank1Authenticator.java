package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Preconditions;
import com.nimbusds.srp6.SRP6ClientCredentials;
import com.nimbusds.srp6.SRP6ClientSession;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6Exception;
import java.math.BigInteger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Identity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.FinishAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.FinishAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.InitiateAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.RestRootResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.AgreementsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.FinishActivationResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticatorNO;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class Sparebank1Authenticator implements BankIdAuthenticatorNO, AutoAuthenticator {

    private static final AggregationLogger log =
            new AggregationLogger(Sparebank1Authenticator.class);

    private final Sparebank1ApiClient apiClient;
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;
    private final RestRootResponse restRootResponse;
    private int pollWaitCounter;

    public Sparebank1Authenticator(
            Sparebank1ApiClient apiClient,
            Credentials credentials,
            PersistentStorage persistentStorage,
            RestRootResponse restRootResponse) {
        this.apiClient = apiClient;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.restRootResponse = restRootResponse;
    }

    @Override
    public String init(String nationalId, String dob, String mobilenumber)
            throws BankIdException, LoginException {
        pollWaitCounter = 0;

        apiClient.initActivation();
        String loginDispatcherHtmlString = apiClient.getLoginDispatcher();
        apiClient.postLoginInformation(loginDispatcherHtmlString, nationalId);

        String selectMarketAndAuthenticationHtmlString = apiClient.selectMarketAndAuthentication();

        String bankIdHtmlResponse =
                apiClient.initBankId(selectMarketAndAuthenticationHtmlString, mobilenumber, dob);

        Document doc = Jsoup.parse(bankIdHtmlResponse);
        Element pollingElement = doc.getElementById("bim-polling");

        if (pollingElement == null) {
            handleBankIdError(doc);
        }

        return pollingElement.select("h1").first().text();
    }

    private void handleBankIdError(Document doc) throws LoginException, BankIdException {
        handleKnownBankIdErrors(
                doc.getElementsByClass("bid-error-wrapper")
                        .first()
                        .select("input")
                        .first()
                        .val()
                        .toLowerCase());

        String errorMessage =
                doc.getElementsByClass("infobox-warning")
                        .first()
                        .getElementsByClass("infobox-content")
                        .first()
                        .select("li")
                        .first()
                        .text();

        throw new IllegalStateException(
                String.format("Could not initiate bankID: %s", errorMessage));
    }

    private void handleKnownBankIdErrors(String bankIdErrorCode)
            throws LoginException, BankIdException {
        switch (bankIdErrorCode) {
            case Sparebank1Constants.BankIdErrorCodes.C161:
                throw LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE.exception();
            case Sparebank1Constants.BankIdErrorCodes.C167:
                throw BankIdError.INVALID_STATUS_OF_MOBILE_BANKID_CERTIFICATE.exception();
        }
    }

    @Override
    public BankIdStatus collect() throws AuthenticationException, AuthorizationException {
        try {
            PollBankIdResponse pollResponse = apiClient.pollBankId();
            String pollStatus = pollResponse.getPollStatus();

            if (Sparebank1Constants.BankIdStatuses.WAITING.equalsIgnoreCase(pollStatus)) {
                pollWaitCounter++;
                return BankIdStatus.WAITING;
            } else if (Sparebank1Constants.BankIdStatuses.COMPLETE.equalsIgnoreCase(pollStatus)) {
                continueActivation();
                return BankIdStatus.DONE;
            } else {
                log.info(
                        String.format(
                                "%s: Unknown poll status: %s",
                                Sparebank1Constants.Tags.BANKID_POLL_UNKNOWN_STATUS, pollStatus));
                return BankIdStatus.FAILED_UNKNOWN;
            }
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatusCodes.STATUS_CODE_SERVER_ERROR) {
                // 500 + more than 20 poll requests means it should be a timeout
                if (pollWaitCounter >= 20) {
                    return BankIdStatus.TIMEOUT;
                }
                // 500 + at least one successful poll request means it should be a user cancellation
                if (pollWaitCounter > 0) {
                    return BankIdStatus.CANCELLED;
                }
            }

            throw e;
        }
    }

    private void continueActivation() throws AuthenticationException {
        apiClient.loginDone();
        apiClient.continueActivation();

        handleAgreementSession();

        Sparebank1Identity identity = finishActivationAndGetIdentity();
        authenticateWithSRP(identity);
    }

    private void handleAgreementSession() throws AuthenticationException {
        AgreementsResponse agreementsResponse = apiClient.getAgreement();

        if (agreementsResponse.getAgreements().isEmpty()) {
            throw LoginError.NOT_CUSTOMER.exception();
        }

        apiClient.finishAgreementSession(agreementsResponse);
    }

    private Sparebank1Identity finishActivationAndGetIdentity() {
        Sparebank1Identity identity =
                Sparebank1Identity.create(credentials.getField(Field.Key.USERNAME));

        LinkEntity challengeLink =
                Preconditions.checkNotNull(
                        restRootResponse.getLinks().get(Sparebank1Constants.Keys.CHALLENGE_KEY),
                        "Challenge link not find.");

        FinishActivationResponse activateUserResponse =
                apiClient.finishActivation(identity, challengeLink.getHref());

        identity.setToken(activateUserResponse.getRememberMeToken());
        identity.save(persistentStorage);

        return identity;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        authenticateWithSRP(Sparebank1Identity.load(persistentStorage));
    }

    private void authenticateWithSRP(Sparebank1Identity identity)
            throws SessionException, BankServiceException {
        SRP6ClientSession clientSession = new SRP6ClientSession();

        LinkEntity loginLink =
                Preconditions.checkNotNull(
                        restRootResponse.getLinks().get(Sparebank1Constants.Keys.LOGIN_KEY),
                        "Login link not found");

        InitiateAuthenticationResponse initAuthResponse =
                apiClient.initAuthentication(identity, loginLink.getHref());

        FinishAuthenticationRequest finishAuthRequest =
                createFinishAuthenticationRequest(clientSession, initAuthResponse, identity);
        LinkEntity validateSessionLink =
                Preconditions.checkNotNull(
                        initAuthResponse
                                .getLinks()
                                .get(Sparebank1Constants.Keys.VALIDATE_SESSION_KEY),
                        "Validate session key link not found");

        FinishAuthenticationResponse step2Response =
                apiClient.finishAuthentication(validateSessionLink.getHref(), finishAuthRequest);

        validateServerEvidenceMesssage(clientSession, step2Response.getM2());
    }

    private FinishAuthenticationRequest createFinishAuthenticationRequest(
            SRP6ClientSession clientSession,
            InitiateAuthenticationResponse authResponse,
            Sparebank1Identity identity) {

        SRP6ClientCredentials srp6credentials =
                computeSRPClientCredentials(clientSession, authResponse, identity);

        FinishAuthenticationRequest request = new FinishAuthenticationRequest();
        request.setPublicA(String.valueOf(srp6credentials.A));
        request.setM1(String.valueOf(srp6credentials.M1));

        return request;
    }

    private SRP6ClientCredentials computeSRPClientCredentials(
            SRP6ClientSession clientSession,
            InitiateAuthenticationResponse authResponse,
            Sparebank1Identity identity) {
        clientSession.step1(identity.getToken(), identity.getPassword());
        SRP6CryptoParams config = SRP6CryptoParams.getInstance(1024, "SHA-256");
        SRP6ClientCredentials cred;

        try {
            cred =
                    clientSession.step2(
                            config,
                            new BigInteger(authResponse.getSalt()),
                            new BigInteger(authResponse.getPublicB()));
        } catch (SRP6Exception e) {
            throw new IllegalStateException(e);
        }

        return cred;
    }

    private void validateServerEvidenceMesssage(SRP6ClientSession clientSession, String m2) {
        try {
            clientSession.step3(new BigInteger(m2));
        } catch (SRP6Exception e) {
            throw new IllegalStateException("Server not authenticated", e);
        }
    }
}
