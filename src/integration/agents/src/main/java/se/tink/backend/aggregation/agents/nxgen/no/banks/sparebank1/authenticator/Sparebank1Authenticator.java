package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator;

import com.google.api.client.http.HttpStatusCodes;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.srp6.SRP6ClientCredentials;
import com.nimbusds.srp6.SRP6ClientSession;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6Exception;
import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.BankIdStatuses;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Claims;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.DeviceValues;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Encryption;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.FormParams;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Keys;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Tags;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Identity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.DeviceInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.PinSrpDataEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.AgreementsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.BankBranchResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.BankBranchResponse.Branch;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.InitBankIdParams;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.InitSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.InitSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.InitTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticatorNO;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
@Slf4j
public class Sparebank1Authenticator implements BankIdAuthenticatorNO, AutoAuthenticator {

    private final Sparebank1ApiClient apiClient;
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;
    private final String branchId;
    private int pollWaitCounter;

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        Sparebank1Identity identity = Sparebank1Identity.load(persistentStorage);
        if (!identity.isAutoAuthenticationPossible()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        manageSession(identity);
    }

    @Override
    public String init(String nationalId, String dob, String mobilenumber)
            throws BankIdException, LoginException {
        pollWaitCounter = 0;
        credentials.setSensitivePayload(Keys.DOB, dob);
        credentials.setSensitivePayload(Keys.NATIONAL_ID, nationalId);
        apiClient.retrieveSessionCookie();

        String bankIdHtml = apiClient.initLogin();

        InitBankIdParams bankIdParams = ScreenScrapingManager.getBankIdInitParams(bankIdHtml);

        String bankIdInitBody = genBankIdInitBody(dob, mobilenumber, bankIdParams);

        String startBankIdHtml = apiClient.selectMarketAndAuthentication(bankIdInitBody);

        return ScreenScrapingManager.getPollingElement(startBankIdHtml);
    }

    private String genBankIdInitBody(
            String dob, String mobilenumber, InitBankIdParams bankIdParams) {
        return Form.builder()
                .put(bankIdParams.getFormId(), bankIdParams.getFormId())
                .put(FormParams.BANKID_MOBILE_NUMBER, mobilenumber)
                .put(FormParams.BANKID_MOBILE_BIRTHDATE, dob)
                .put(FormParams.NESTE_MOBIL, FormParams.NESTE)
                .put(FormParams.JAVAX_FACES_VIEW_STATE, bankIdParams.getViewState())
                .build()
                .serialize();
    }

    @Override
    public BankIdStatus collect() throws AuthenticationException, AuthorizationException {
        try {
            PollBankIdResponse pollResponse = apiClient.pollBankId();
            String pollStatus = pollResponse.getPollStatus();

            if (BankIdStatuses.WAITING.equalsIgnoreCase(pollStatus)) {
                pollWaitCounter++;
                return BankIdStatus.WAITING;
            } else if (BankIdStatuses.COMPLETE.equalsIgnoreCase(pollStatus)) {
                return BankIdStatus.DONE;
            } else {
                log.info(
                        String.format(
                                "%s: Unknown poll status: %s",
                                Tags.BANKID_POLL_UNKNOWN_STATUS, pollStatus));
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

    @Override
    public void finishActivation() throws SupplementalInfoException {
        Sparebank1Identity identity = Sparebank1Identity.create();

        apiClient.loginDone();
        apiClient.requestDigitalSession();

        manageBankBranch();
        manageAgreements();
        manageToken(identity);
        identity.save(persistentStorage);
        manageSession(identity);
    }

    private void manageBankBranch() {
        BankBranchResponse response = apiClient.getUserBranches();

        Branch bankBranch =
                response.getBanks().stream()
                        .filter(branch -> branchId.equals(branch.getId()))
                        .findFirst()
                        .orElseThrow(LoginError.NOT_CUSTOMER::exception);

        apiClient.setSpecificUserBranch(bankBranch);
    }

    private void manageAgreements() throws AuthenticationException {
        AgreementsResponse agreementsResponse = apiClient.getAgreements();
        if (agreementsResponse.getAgreements().isEmpty()) {
            throw LoginError.NOT_CUSTOMER.exception();
        }

        if (agreementsResponse.getAgreements().size() > 1) {
            log.info("There are more than 1 agreement for this branch");
        }
        // In case of multiple agreements first will be taken.
        apiClient.setSpecificAgreement(agreementsResponse.getAgreements().get(0).getAgreementId());
    }

    private void manageToken(Sparebank1Identity identity) {
        String signedJwt = generateJwt(identity);
        TokenResponse response = apiClient.requestForToken(new InitTokenRequest(signedJwt));
        identity.setToken(response.getRememberMeToken());
    }

    @SneakyThrows
    private String generateJwt(Sparebank1Identity identity) {
        byte[] secret = Encryption.KEY.getBytes();
        JWSSigner signer = new MACSigner(secret);
        JWTClaimsSet claimsSet = buildClaims(identity);
        JWSHeader header =
                new JWSHeader.Builder(JWSAlgorithm.HS512)
                        .keyID("2021-03")
                        .type(JOSEObjectType.JWT)
                        .build();
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    private JWTClaimsSet buildClaims(Sparebank1Identity identity) {
        DeviceInfoEntity deviceInfo = DeviceInfoEntity.create();
        long timestamp =
                apiClient.requestForTokenExpirationTimestamp().getTokenExpirationTimestamp();
        PinSrpDataEntity pinSrpDataEntity = PinSrpDataEntity.create(identity);
        return new JWTClaimsSet.Builder()
                .claim(Claims.DEVICE_ID, identity.getDeviceId())
                .claim(Claims.DEVICE_DESCRIPTION, DeviceValues.DESCRIPTION)
                .claim(Claims.BASE_64_ENCODED_PUBLIC_KEY, identity.getUserName())
                .claim(Claims.EXP, timestamp)
                .claim(Claims.TYPE, DeviceValues.STRONG)
                .claim(Claims.DEVICE_INFO, deviceInfo)
                .claim(Claims.PIN_SRP_DATA, pinSrpDataEntity)
                .build();
    }

    private void manageSession(Sparebank1Identity identity)
            throws SessionException, BankServiceException {

        SRP6ClientSession clientSession = new SRP6ClientSession();

        InitSessionRequest initSessionRequest = InitSessionRequest.create(identity);
        InitSessionResponse initSessionResponse = apiClient.initiateSession(initSessionRequest);

        SessionRequest sessionRequest =
                createFinishSessionInitiationRequest(clientSession, initSessionResponse, identity);
        SessionResponse step2Response = apiClient.finishSessionInitiation(sessionRequest);

        validateServerEvidenceMesssage(clientSession, step2Response.getM2());
    }

    private SessionRequest createFinishSessionInitiationRequest(
            SRP6ClientSession clientSession,
            InitSessionResponse initSessionResponse,
            Sparebank1Identity identity) {

        SRP6ClientCredentials srp6credentials =
                computeSRPClientCredentials(clientSession, initSessionResponse, identity);

        return new SessionRequest(
                String.valueOf(srp6credentials.M1),
                String.valueOf(srp6credentials.A),
                identity.getToken(),
                "pin");
    }

    @SneakyThrows
    private SRP6ClientCredentials computeSRPClientCredentials(
            SRP6ClientSession clientSession,
            InitSessionResponse initSessionResponse,
            Sparebank1Identity identity) {
        clientSession.step1(identity.getToken(), identity.getPassword());
        SRP6CryptoParams config = SRP6CryptoParams.getInstance(1024, "SHA-256");
        SRP6ClientCredentials cred;
        cred =
                clientSession.step2(
                        config,
                        new BigInteger(initSessionResponse.getSalt()),
                        new BigInteger(initSessionResponse.getPublicB()));

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
