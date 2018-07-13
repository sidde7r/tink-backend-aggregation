package se.tink.backend.aggregation.agents.banks.uk.barclays;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.security.KeyPair;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.banks.uk.barclays.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.ConfigurationRequest;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.EmptyRequest;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Request;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Response;
import se.tink.backend.aggregation.agents.banks.uk.barclays.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.account.AccountListResponse;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.transaction.TransactionListRequest;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.transaction.TransactionListResponse;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.authentication.AuthenticationStep1Request;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.authentication.AuthenticationStep1Response;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.authentication.AuthenticationStep2Request;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.authentication.AuthenticationStep2Response;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.deviceregistration.DeviceRegStep1Request;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.deviceregistration.DeviceRegStep1Response;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.deviceregistration.DeviceRegStep2Request;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration.PinSentryChallengeRequest;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration.PinSentryInitRequest;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration.PinSentryInitResponse;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration.SmsChallengeRequest;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration.SmsInitRequest;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration.UserInformationRequest;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration.UserInformationResponse;
import se.tink.backend.aggregation.agents.utils.jersey.WebResourceBuilderFactory;

class BarclaysApiClient implements WebResourceBuilderFactory {
    private final String userId;
    private final Client client;
    private final String deviceIdentifier;
    private final String userAgent = "Barclays/175.0.41 CFNetwork/790.2 Darwin/16.0.0";
    private BarclaysSession session;


    public BarclaysApiClient(String userId, Client client, String deviceIdentifier) {
        this.userId = userId;
        this.client = client;
        this.deviceIdentifier = deviceIdentifier;
    }

    public BarclaysSession getSession() {
        return session;
    }

    public void setSession(BarclaysSession session) {
        this.session = session;
    }

    @Override
    public WebResource.Builder createWebResourceBuilder(String url) throws Exception {
        return this.client.resource(url).accept(MediaType.WILDCARD_TYPE);
    }

    private byte[] post(String path, byte[] data) throws Exception {
        String url = String.format("%s%s", BarclaysConstants.BASE_URL, path);
        WebResource.Builder clientRequest = createWebResourceBuilder(url)
                .type("application/binary")
                .header("User-Agent", userAgent);

        return clientRequest.post(byte[].class, data);
    }

    private <T extends Response> T doRequest(BarclaysSession session, String path, Request request, Class<T> resType) {
        try {
            byte[] requestData = BarclaysUtils.serializeRequest(session, request);
            byte[] responseData = post(path, requestData);
            return BarclaysUtils.deserializeResponse(session, responseData, resType);
        } catch(Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public BarclaysIdentity registerDevice() {
        // The first thing we must do as a new device is to register it.
        // We will send two EC keys and be given a temporary `aid` (the final aid will be given in the `registerUser()`).
        BarclaysIdentity identity = new BarclaysIdentity(userId, deviceIdentifier);
        BarclaysSession tmpSession = new BarclaysSession();

        DeviceRegStep1Request regStep1Req = new DeviceRegStep1Request();
        regStep1Req.setDeviceId(identity.getDeviceId());
        DeviceRegStep1Response regStep1Response = doRequest(
                tmpSession,
                BarclaysConstants.REGISTRATION_URL_PATH,
                regStep1Req,
                DeviceRegStep1Response.class);
        if (regStep1Response.isError()) {
            throw new IllegalStateException("devReg step1: " + regStep1Response.getErrorCode());
        }
        identity.setaId(regStep1Response.getAid());
        tmpSession.setSessionId(regStep1Response.getSid());

        DeviceRegStep2Request regStep2Req = new DeviceRegStep2Request(identity.getQsa(), identity.getQsd());
        Response regStep2Res = doRequest(
                tmpSession,
                BarclaysConstants.REGISTRATION_URL_PATH,
                regStep2Req,
                Response.class);
        if (regStep2Res.isError()) {
            throw new IllegalStateException("devReg step2: " + regStep2Res.getErrorCode());
        }
        return identity;
    }


    public BarclaysSession authenticateDevice(BarclaysIdentity identity) {
        BarclaysSession tmpSession = new BarclaysSession();
        KeyPair EcKeyExchange = BarclaysCrypto.ecGenerateKeyPair();

        AuthenticationStep1Request authStep1Req = new AuthenticationStep1Request();
        authStep1Req.setDeviceId(identity.getDeviceId());
        authStep1Req.setaId(identity.getaId());
        authStep1Req.setQea(EcKeyExchange);
        AuthenticationStep1Response authStep1Res = doRequest(
                tmpSession,
                BarclaysConstants.AUTHENTICATION_URL_PATH,
                authStep1Req,
                AuthenticationStep1Response.class);
        if (authStep1Res.isError()) {
            throw new IllegalStateException("devAuth step1: " + authStep1Res.getErrorCode());
        }
        tmpSession.setSessionId(authStep1Res.getSid());

        AuthenticationStep2Request authStep2Req = new AuthenticationStep2Request();
        // Dummy signature; don't know why they do this. Could be a place holder for a future signature.
        authStep2Req.setSignature2(BarclaysUtils.generateDummySignature());
        authStep2Req.setSignature1(BarclaysCrypto.ecSignSha256(identity.getQsa(), authStep1Res.get__dataHash__()));

        // Calculate the authenticated session keys
        byte[] sharedSecret = BarclaysCrypto.ecdhDerive(EcKeyExchange, authStep1Res.getQeb());
        byte[] iv = Bytes.concat(identity.getaId().getBytes(Charsets.UTF_8), BarclaysConstants.RSA_PUB_KEY);
        BarclaysSession authenticatedSession = new BarclaysSession(sharedSecret, iv);
        authStep2Req.setKeyConfirmationMsg(authenticatedSession.getKeyConfirmationMsg());

        AuthenticationStep2Response authStep2Res = doRequest(
                tmpSession,
                BarclaysConstants.AUTHENTICATION_URL_PATH,
                authStep2Req,
                AuthenticationStep2Response.class);
        if (authStep2Res.isError()) {
            throw new IllegalStateException("devAuth step2: " + authStep2Res.getErrorCode());
        }
        authenticatedSession.setAuthenticated(true);
        authenticatedSession.setSessionId(tmpSession.getSessionId());
        return authenticatedSession;
    }

    public void queryConfiguration() {
        // we must query the configuration when we register and login
        ConfigurationRequest configurationRequest = new ConfigurationRequest();
        Response configurationResponse = doRequest(
                session,
                BarclaysConstants.SESSION_URL_PATH,
                configurationRequest,
                Response.class);
        if (configurationResponse.isError()) {
            throw new IllegalStateException("configurationResponse error: " + configurationResponse.getErrorCode());
        }
    }

    public UserInformationResponse submitUserInformation(String firtName, String lastName, String sortCode,
            String accountNumber, String phoneNumber) {
        UserInformationRequest userInformationRequest = new UserInformationRequest();
        userInformationRequest.setFirstName(firtName);
        userInformationRequest.setLastName(lastName);
        userInformationRequest.setSortCode(sortCode);
        userInformationRequest.setAccountNumber(accountNumber);
        userInformationRequest.setPhoneNumber(phoneNumber);

        UserInformationResponse userInformationResponse = doRequest(
                session,
                BarclaysConstants.SESSION_URL_PATH,
                userInformationRequest,
                UserInformationResponse.class);
        if (userInformationResponse.isError()) {
            throw new IllegalStateException("submitUserInformation: " + userInformationResponse.getErrorCode());
        }
        return userInformationResponse;
    }

    public void initiateSmsChallenge() {
        SmsInitRequest smsInitRequest = new SmsInitRequest();
        Response smsInitResponse = doRequest(
                session,
                BarclaysConstants.SESSION_URL_PATH,
                smsInitRequest,
                Response.class);
        if (smsInitResponse.isError()) {
            throw new IllegalStateException("initiateSmsChallenge: " + smsInitResponse.getErrorCode());
        }
    }

    public void respondSmsChallenge(String smsVerificationCode) {
        SmsChallengeRequest smsChallengeRequest = new SmsChallengeRequest(smsVerificationCode);
        Response smsVerificationResponse = doRequest(
                session,
                BarclaysConstants.SESSION_URL_PATH,
                smsChallengeRequest,
                Response.class);
        if (smsVerificationResponse.isError()) {
            throw new IllegalStateException("respondSmsChallenge: " + smsVerificationResponse.getErrorCode());
        }
    }

    public PinSentryInitResponse initiatePinSentryChallenge() {
        PinSentryInitRequest pinSentryInitRequest = new PinSentryInitRequest();
        PinSentryInitResponse pinSentryInitResponse = doRequest(
                session,
                BarclaysConstants.SESSION_URL_PATH,
                pinSentryInitRequest,
                PinSentryInitResponse.class);
        if (pinSentryInitResponse.isError()) {
            throw new IllegalStateException("initiatePinSentryChallenge: " + pinSentryInitResponse.getErrorCode());
        }
        return pinSentryInitResponse;
    }

    public void respondPinSentryChallenge(String cardLastFourDigits, String pinSentryResponse) {
        PinSentryChallengeRequest pinSentryChallengeRequest = new PinSentryChallengeRequest();
        pinSentryChallengeRequest.setLastFourDigits(cardLastFourDigits);
        pinSentryChallengeRequest.setChallengeResponse(pinSentryResponse);

        Response pinSentryChallengeResponse = doRequest(
                session,
                BarclaysConstants.SESSION_URL_PATH,
                pinSentryChallengeRequest,
                Response.class);
        if (pinSentryChallengeResponse.isError()) {
            throw new IllegalStateException("respondPinSentryChallenge: " + pinSentryChallengeResponse.getErrorCode());
        }
    }

    public List<AccountEntity> fetchAccounts() {
        EmptyRequest accountListRequest = new EmptyRequest("BA01C");
        AccountListResponse accountListResponse = doRequest(
                session,
                BarclaysConstants.SESSION_URL_PATH,
                accountListRequest,
                AccountListResponse.class);
        if (accountListResponse.isError()) {
            throw new IllegalStateException("accountListResponse error: " + accountListResponse.getErrorCode());
        }
        return accountListResponse.getAccounts();
    }

    public List<TransactionEntity> fetchTransactions(String accountIdentifier) {
        TransactionListRequest transactionListRequest = new TransactionListRequest(accountIdentifier);
        TransactionListResponse transactionListResponse = doRequest(
                session,
                BarclaysConstants.SESSION_URL_PATH,
                transactionListRequest,
                TransactionListResponse.class);
        if (transactionListResponse.isError()) {
            throw new IllegalStateException("transactionListResponse error: " + transactionListResponse.getErrorCode());
        }
        return transactionListResponse.getTransactions();
    }
}
