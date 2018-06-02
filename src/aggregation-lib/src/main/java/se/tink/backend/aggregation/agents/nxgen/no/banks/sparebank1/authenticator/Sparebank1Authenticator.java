package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator;

import com.google.common.base.Objects;
import com.nimbusds.srp6.SRP6ClientCredentials;
import com.nimbusds.srp6.SRP6ClientSession;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6Exception;
import java.math.BigInteger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.UrlParameter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Identity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation.DeviceInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation.PinSrpDataEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.FinishAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.FinishAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.InitiateAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.InitiateAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.RestRootResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.AgreementsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.FinishActivationRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.FinishActivationResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.InitBankIdBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.InitLoginBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.TargetUrlRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation.TargetUrlResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.FinancialInstitutionEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticatorNO;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.utils.StringUtils;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Urls;

public class Sparebank1Authenticator implements BankIdAuthenticatorNO, AutoAuthenticator {

    private static final AggregationLogger log = new AggregationLogger(Sparebank1Authenticator.class);

    private final Sparebank1ApiClient apiClient;
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;
    private final String bankKey;
    private final String bankName;
    private final FinancialInstitutionEntity financialInstitution;
    private final RestRootResponse restRootResponse;
    private int pollWaitCounter;

    public Sparebank1Authenticator(Sparebank1ApiClient apiClient, Credentials credentials,
            PersistentStorage persistentStorage, String bankKey, FinancialInstitutionEntity financialInstitution,
            RestRootResponse restRootResponse) {
        this.apiClient = apiClient;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.bankKey = bankKey;
        // Remove "fid-" prefix from the bankKey
        this.bankName = bankKey.substring(4);
        this.financialInstitution = financialInstitution;
        this.restRootResponse = restRootResponse;
    }
    @Override
    public String init(String nationalId, String dob, String mobilenumber) throws BankIdException, LoginException {
        pollWaitCounter = 0;

        apiClient.get(financialInstitution.getLinks().get(Sparebank1Constants.ACTIVATION_KEY).getHref(), HttpResponse.class);
        String loginHtmlResponse = apiClient.get(getLoginDispatcherUrl(), String.class);
        InitLoginBody loginBody = getLoginBody(loginHtmlResponse, nationalId);
        apiClient.postLoginInformation(
                Urls.INIT_LOGIN.queryParam(Sparebank1Constants.CID, Sparebank1Constants.CID_VALUE), loginBody);

        String initBankIdHtmlResponse = apiClient.get(getSelectMarketAndAuthTypeUrl(), String.class);
        InitBankIdBody initBankIdBody = getInitBankIdBody(initBankIdHtmlResponse, mobilenumber, dob);

        String bankIdHtmlResponse = apiClient.initBankIdLogin(
                Urls.SELECT_MARKET_AND_AUTH_TYPE.queryParam(
                        Sparebank1Constants.CID, Sparebank1Constants.CID_VALUE), initBankIdBody);

        Document doc = Jsoup.parse(bankIdHtmlResponse);
        Element pollingElement = doc.getElementById("bim-polling");

        if (pollingElement != null) {
            return pollingElement.select("h1").first().text();
        } else {
            handleKnownBankIdErrors(doc.getElementsByClass("bid-error-wrapper").first()
                    .select("input").first().val().toLowerCase());

            String errorMessage = doc.getElementsByClass("infobox-warning").first()
                    .getElementsByClass("infobox-content").first()
                    .select("li").first().text();

            throw new IllegalStateException(String.format("Could not initiate bankID: %s",
                    errorMessage));
        }
    }

    private URL getLoginDispatcherUrl() {
        return Urls.GET_LOGIN_DISPATCHER
                .queryParam("app", "mobilbank")
                .queryParam("finInst", bankKey)
                .queryParam("goto", Urls.CONTINUE_ACTIVATION.toString());
    }

    private InitLoginBody getLoginBody(String htmlResponse, String nationalId) {
        Document loginDoc = Jsoup.parse(htmlResponse);
        String viewStateValuesLogin = loginDoc.getElementById("j_id1:javax.faces.ViewState:0").val();

        return new InitLoginBody(nationalId, viewStateValuesLogin);
    }

    private URL getSelectMarketAndAuthTypeUrl() {
        return Urls.SELECT_MARKET_AND_AUTH_TYPE
                .queryParam("app", "mobilbank")
                .queryParam("finInst", bankKey)
                .queryParam("market", "PRIVATE")
                .queryParam("goto", "https://mobilbank-pm.sparebank1.no/personal/activation/continue-activation&cid=1");
    }

    private InitBankIdBody getInitBankIdBody(String htmlResponse, String mobilenumber, String dob) {
        Document initBankIdDoc = Jsoup.parse(htmlResponse);
        Element form = initBankIdDoc.getElementById("panel-bankID-mobile").select("form").first();
        String formId = form.id();
        String viewStateValuesInitBankId = form.getElementById("j_id1:javax.faces.ViewState:1").val();

        return new InitBankIdBody(mobilenumber, dob, formId, viewStateValuesInitBankId);
    }

    private void handleKnownBankIdErrors(String bankIdErrorCode) throws LoginException, BankIdException {
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
            HttpResponse httpResponse = apiClient.pollBankId(
                    Urls.POLL_BANKID.queryParam(Sparebank1Constants.CID, Sparebank1Constants.CID_VALUE));

            PollBankIdResponse pollResponse = httpResponse.getBody(PollBankIdResponse.class);
            String pollStatus = pollResponse.getPollStatus();

            if (Objects.equal(pollStatus.toLowerCase(), "waiting")) {
                pollWaitCounter++;
                return BankIdStatus.WAITING;
            } else if (Objects.equal(pollStatus.toLowerCase(), "complete")) {
                continueActivation();
                return BankIdStatus.DONE;
            } else {
                log.info(String.format("%s - Sparebank1 - Unknown poll status: %s",
                        Sparebank1Constants.LOG_BANKID_POLL_UNKNOWN_STATUS_TAG,
                        pollStatus));
                return BankIdStatus.FAILED_UNKNOWN;
            }
        } catch (HttpResponseException e) {
            if (Objects.equal(e.getResponse().getStatus(), 500)) {
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
        apiClient.get(Urls.LOGIN_DONE.queryParam(Sparebank1Constants.CID, Sparebank1Constants.CID_VALUE),
                HttpResponse.class);
        apiClient.continueActivation(Urls.CONTINUE_ACTIVATION);

        handleAgreementSession();

        Sparebank1Identity identity = finishActivationAndGetIdentity();
        authenticateWithSRP(identity);
    }

    private void handleAgreementSession() throws AuthenticationException {
        AgreementsResponse agreementsResponse = apiClient.getAgreement(Urls.AGREEMENTS.parameter(
                UrlParameter.BANK_NAME, bankName));

        if (agreementsResponse.getAgreements().isEmpty()) {
            throw LoginError.NOT_CUSTOMER.exception();
        }

        TargetUrlRequest targetUrlRequest = getTargetUrlRequest(agreementsResponse);
        TargetUrlResponse targetUrlResponse = apiClient.finishAgreementSession(
                Urls.AGREEMENTS.parameter(UrlParameter.BANK_NAME, bankName), targetUrlRequest, bankName);
        apiClient.get(new URL(targetUrlResponse.getTargetUrl()), HttpResponse.class);
    }

    private TargetUrlRequest getTargetUrlRequest(AgreementsResponse agreementsResponse) {
        TargetUrlRequest targetUrlRequest = new TargetUrlRequest();
        targetUrlRequest.setAgreementId(agreementsResponse.getAgreements().get(0).getAgreementId());

        return targetUrlRequest;
    }

    private Sparebank1Identity finishActivationAndGetIdentity() {
        Sparebank1Identity identity = createIdentity();

        FinishActivationRequest finishActivationRequest = createActivateUserRequest(identity);
        FinishActivationResponse activateUserResponse = apiClient.finishActivation(
                restRootResponse.getLinks()
                        .get(Sparebank1Constants.CHALLENGE_KEY).getHref(), finishActivationRequest);

        identity.setToken(activateUserResponse.getRememberMeToken());
        identity.save(persistentStorage);

        return identity;
    }

    private Sparebank1Identity createIdentity() {
        String deviceId = StringUtils.hashAsUUID("TINK-" + credentials.getField(Field.Key.USERNAME));
        return Sparebank1Identity.create(deviceId);
    }

    private FinishActivationRequest createActivateUserRequest(Sparebank1Identity identity) {
        DeviceInfoEntity deviceInfoEntity = createDeviceInfoEntity();
        PinSrpDataEntity pinSrpDataEntity = createPinSrpDataEntity(identity);

        FinishActivationRequest request = new FinishActivationRequest();
        request.setDeviceDescription(Sparebank1Constants.DEVICE_DESCRIPTION);
        request.setDeviceId(identity.getDeviceId());
        request.setBase64EncodedPublicKey(identity.getUserName());
        request.setDeviceInfo(deviceInfoEntity);
        request.setType("strong");
        request.setPinSrpData(pinSrpDataEntity);

        return request;
    }

    private DeviceInfoEntity createDeviceInfoEntity() {
        DeviceInfoEntity deviceInfoEntity = new DeviceInfoEntity();
        deviceInfoEntity.setManufacturer(Sparebank1Constants.DEVICE_MANUFACTURER);
        deviceInfoEntity.setModel(Sparebank1Constants.DEVICE_MODEL);

        return deviceInfoEntity;
    }

    private PinSrpDataEntity createPinSrpDataEntity(Sparebank1Identity identity) {
        PinSrpDataEntity pinSrpDataEntity = new PinSrpDataEntity();
        pinSrpDataEntity.setSalt(String.valueOf(identity.getSalt()));
        pinSrpDataEntity.setUsername(identity.getUserName());
        pinSrpDataEntity.setVerificator(String.valueOf(identity.getVerificator()));

        return pinSrpDataEntity;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        authenticateWithSRP(loadIdentity());
    }

    private Sparebank1Identity loadIdentity() {
        Sparebank1Identity identity = new Sparebank1Identity();
        identity.load(persistentStorage);
        return identity;
    }

    private void authenticateWithSRP(Sparebank1Identity identity) {
        SRP6ClientSession clientSession = new SRP6ClientSession();

        InitiateAuthenticationRequest initRequest = createInitiateAuthenticationRequest(identity);
        InitiateAuthenticationResponse initAuthResponse = apiClient.initAuthentication(
                restRootResponse.getLinks().get(Sparebank1Constants.LOGIN_KEY).getHref(), initRequest);

        FinishAuthenticationRequest finishAuthRequest = createFinishAuthenticationRequest(
                clientSession, initAuthResponse, identity);

        FinishAuthenticationResponse step2Response = apiClient.finishAuthentication(
                initAuthResponse.getLinks().get("validateSessionKey").getHref(),
                finishAuthRequest);

        validateServerEvidenceMesssage(clientSession, step2Response.getM2());
    }

    private InitiateAuthenticationRequest createInitiateAuthenticationRequest(Sparebank1Identity identity) {
        InitiateAuthenticationRequest request = new InitiateAuthenticationRequest();
        request.setToken(identity.getToken());
        request.setDeviceId(identity.getDeviceId());
        request.setAuthenticationMethod("pin");

        return request;
    }

    private FinishAuthenticationRequest createFinishAuthenticationRequest(SRP6ClientSession clientSession,
            InitiateAuthenticationResponse authResponse, Sparebank1Identity identity) {
        SRP6ClientCredentials cred = computeSRPClientCredentials(clientSession, authResponse, identity);
        FinishAuthenticationRequest request = new FinishAuthenticationRequest();
        request.setPublicA(String.valueOf(cred.A));
        request.setM1(String.valueOf(cred.M1));

        return request;
    }

    private SRP6ClientCredentials computeSRPClientCredentials(SRP6ClientSession clientSession,
            InitiateAuthenticationResponse authResponse, Sparebank1Identity identity) {
        clientSession.step1(identity.getToken(), identity.getPassword());
        SRP6CryptoParams config = SRP6CryptoParams.getInstance(1024, "SHA-256");
        SRP6ClientCredentials cred;

        try {
            cred = clientSession.step2(config, new BigInteger(authResponse.getSalt()),
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
