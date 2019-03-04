package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.GenerateChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.GenerateChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.GenerateOtpChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.GenerateOtpChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.LogonResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.RegisterUserRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.RegisterUserResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.StoreRegistrationCdRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.StoreRegistrationCdResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc.GetAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.session.rpc.PendingRequestsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.session.rpc.PendingRequestsResponse;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

import java.util.UUID;

public final class AxaApiClient {

    private final TinkHttpClient httpClient;

    public AxaApiClient(TinkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public GenerateChallengeResponse postGenerateChallenge(
            final String basicAuth, final String ucrid) {
        GenerateChallengeRequest request =
                GenerateChallengeRequest.builder()
                        .setUcrid(ucrid)
                        .setApplcd(AxaConstants.Request.APPL_CD)
                        .setLanguage(AxaConstants.Request.LANGUAGE)
                        .build();
        return httpClient
                .request(AxaConstants.Url.GENERATE_CHALLENGE)
                .header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", basicAuth))
                .headers(AxaConstants.HEADERS_JSON)
                .post(GenerateChallengeResponse.class, request);
    }

    public RegisterUserResponse postRegisterUser(
            final String basicAuth,
            final String ucrid,
            final UUID deviceId,
            final String pan,
            final String challenge,
            final String challengeResponse,
            final String clientInitialVector,
            final String encryptedClientPublicKeyAndNonce) {
        final RegisterUserRequest request =
                RegisterUserRequest.builder()
                        .setUcrid(ucrid)
                        .setPanNumberFull(pan)
                        .setChallenge(challenge)
                        .setResponse(challengeResponse)
                        .setClientInitialVector(clientInitialVector)
                        .setEncryptedClientPublicKeyAndNonce(encryptedClientPublicKeyAndNonce)
                        .setApplcd(AxaConstants.Request.APPL_CD)
                        .setDeviceBrand(AxaConstants.Request.BRAND)
                        .setLanguage(AxaConstants.Request.LANGUAGE)
                        .setDeviceId(deviceId)
                        .setModel(AxaConstants.Request.MODEL)
                        .setVersionNumber(AxaConstants.Request.VERSION_NUMBER)
                        .setOperatingSystem(AxaConstants.Request.OPERATING_SYSTEM)
                        .setJailBrokenOrRooted(AxaConstants.Request.JAILBROKEN_OR_ROOTED)
                        .build();
        return httpClient
                .request(AxaConstants.Url.REGISTER_USER)
                .header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", basicAuth))
                .headers(AxaConstants.HEADERS_JSON)
                .post(RegisterUserResponse.class, request);
    }

    public StoreRegistrationCdResponse postStoreDerivation(
            final String basicAuth,
            final String clientInitialVector,
            final String derivationCd,
            final String encryptedServerNonce,
            final String serialNo) {
        final StoreRegistrationCdRequest request =
                StoreRegistrationCdRequest.builder()
                        .setApplcd(AxaConstants.Request.APPL_CD)
                        .setLanguage(AxaConstants.Request.LANGUAGE)
                        .setClientInitialVector(clientInitialVector)
                        .setDerivationCd(derivationCd)
                        .setEncryptedServerNonce(encryptedServerNonce)
                        .setSerialNo(serialNo)
                        .build();
        return httpClient
                .request(AxaConstants.Url.STORE_DERIVATION_CD)
                .header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", basicAuth))
                .headers(AxaConstants.HEADERS_JSON)
                .body(request)
                .post(StoreRegistrationCdResponse.class);
    }

    public GenerateOtpChallengeResponse postGenerateOtpChallenge(
            final String basicAuth, final String serialNo) {
        final GenerateOtpChallengeRequest request =
                GenerateOtpChallengeRequest.builder()
                        .setSerialNo(serialNo)
                        .setApplcd(AxaConstants.Request.APPL_CD)
                        .setLanguage(AxaConstants.Request.LANGUAGE)
                        .build();

        return httpClient
                .request(AxaConstants.Url.GENERATE_OTP_CHALLENGE)
                .header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", basicAuth))
                .headers(AxaConstants.HEADERS_JSON)
                .post(GenerateOtpChallengeResponse.class, request);
    }

    public LogonResponse postLogon(
            final String basicAuth,
            final String deviceId,
            final String username,
            final String password) {
        final Form request =
                AxaConstants.Request.LOGON_BODY
                        .rebuilder()
                        .put(AxaConstants.Request.USERNAME_KEY, username)
                        .put(AxaConstants.Request.PASSWORD_KEY, password)
                        .put(AxaConstants.Request.DEVICEID_KEY, deviceId)
                        .build();
        return httpClient
                .request(AxaConstants.Url.LOGON)
                .header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", basicAuth))
                .headers(AxaConstants.HEADERS_FORM)
                .body(request.serialize())
                .post(LogonResponse.class);
    }

    public PendingRequestsResponse postPendingRequests(
            final int customerId, final String accessToken) {
        final PendingRequestsRequest body =
                PendingRequestsRequest.builder()
                        .setApplCd(AxaConstants.Request.APPL_CD)
                        .setLanguage(AxaConstants.Request.LANGUAGE)
                        .setCustomerId(customerId)
                        .build();
        return httpClient
                .request(AxaConstants.Url.PENDING_PRODUCT_REQUESTS)
                .headers(AxaConstants.HEADERS_JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken))
                .body(body)
                .post(PendingRequestsResponse.class);
    }

    public GetAccountsResponse postGetAccounts(final int customerId, final String accessToken) {
        final GetAccountsRequest body =
                GetAccountsRequest.builder()
                        .setApplCd(AxaConstants.Request.APPL_CD)
                        .setLanguage(AxaConstants.Request.LANGUAGE)
                        .setCustomerId(customerId)
                        .build();
        return httpClient
                .request(AxaConstants.Url.FETCH_ACCOUNTS)
                .headers(AxaConstants.HEADERS_JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken))
                .body(body)
                .post(GetAccountsResponse.class);
    }
}
