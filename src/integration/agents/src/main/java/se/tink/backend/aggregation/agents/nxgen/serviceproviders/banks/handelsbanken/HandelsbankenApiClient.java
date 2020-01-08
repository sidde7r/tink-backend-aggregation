package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.EntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.KeepAliveResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.HandshakeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.HandshakeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ServerProfileRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ServerProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.ActivateProfileRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.ActivateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CommitProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CreateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.InitNewProfileRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.InitNewProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.loan.rpc.HandelsbankenLoansResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public abstract class HandelsbankenApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandelsbankenApiClient.class);

    private final TinkHttpClient client;
    protected final HandelsbankenConfiguration<?> handelsbankenConfiguration;

    public HandelsbankenApiClient(
            TinkHttpClient client, HandelsbankenConfiguration handelsbankenConfiguration) {
        this.client = client;
        this.handelsbankenConfiguration = handelsbankenConfiguration;
    }

    public EntryPointResponse fetchEntryPoint() {
        return createRequest(handelsbankenConfiguration.getEntryPoint())
                .get(EntryPointResponse.class);
    }

    public InitNewProfileResponse initNewProfile(
            EntryPointResponse entryPoint, InitNewProfileRequest initNewProfileRequest) {
        return createPostRequest(entryPoint.toPinnedActivation())
                .post(InitNewProfileResponse.class, initNewProfileRequest);
    }

    public ActivateProfileResponse activateProfile(
            CreateProfileResponse createProfile, ActivateProfileRequest activateProfileRequest) {
        return createPostRequest(createProfile.toActivateProfile())
                .post(ActivateProfileResponse.class, activateProfileRequest);
    }

    public CommitProfileResponse commitProfile(ActivateProfileResponse activateProfile) {
        return createRequest(activateProfile.toCommitProfile()).get(CommitProfileResponse.class);
    }

    public HandshakeResponse handshake(
            EntryPointResponse entrypoint, HandshakeRequest handshakeRequest) {
        return createPostRequest(entrypoint.toPinnedLogin())
                .post(HandshakeResponse.class, handshakeRequest);
    }

    public ServerProfileResponse serverProfile(
            HandshakeResponse handshake, ServerProfileRequest serverProfileRequest) {
        return createPostRequest(handshake.toGetServerProfile())
                .post(ServerProfileResponse.class, serverProfileRequest);
    }

    public ChallengeResponse challenge(
            ServerProfileResponse serverProfile, ChallengeRequest challengeRequest) {
        return createPostRequest(serverProfile.toGetChallenge())
                .post(ChallengeResponse.class, challengeRequest);
    }

    public ValidateSignatureResponse validateSignature(
            ChallengeResponse challenge, ValidateSignatureRequest validateSignature) {
        return createPostRequest(challenge.toValidateSignature())
                .post(ValidateSignatureResponse.class, validateSignature);
    }

    public ApplicationEntryPointResponse applicationEntryPoint(AuthorizeResponse authorize) {
        return createRequest(authorize.toApplicationEntryPoint())
                .get(ApplicationEntryPointResponse.class);
    }

    public KeepAliveResponse keepAlive(ApplicationEntryPointResponse applicationEntryPoint) {
        long start = System.nanoTime();
        try {
            // it's 'only' a ping so Handelsbanken doesn't return anything when alive but returns
            // XML if not...
            HttpResponse httpResponse =
                    createRequest(applicationEntryPoint.toKeepAlive()).get(HttpResponse.class);

            if (httpResponse.hasBody()) {
                long stop = System.nanoTime();
                long total = stop - start;
                long notAliveTime = TimeUnit.SECONDS.convert(total, TimeUnit.NANOSECONDS);
                LOGGER.info("Time in keepAlive when session is not alive: {} s", notAliveTime);
                return httpResponse.getBody(KeepAliveResponse.class);
            }

            long stop = System.nanoTime();
            long total = stop - start;
            long whenAliveTime = TimeUnit.SECONDS.convert(total, TimeUnit.NANOSECONDS);
            LOGGER.info("Time in keepAlive when session is alive: {} s", whenAliveTime);
            return KeepAliveResponse.aliveEntryPoint();
        } catch (HttpResponseException e) {
            LOGGER.warn(
                    HandelsbankenConstants.URLS.KeepAlive.LOG_TAG
                            + " - Keeping session alive failed",
                    e);
            long stop = System.nanoTime();
            long total = stop - start;
            long exceptionTime = TimeUnit.SECONDS.convert(total, TimeUnit.NANOSECONDS);
            LOGGER.info("Time in keepAlive with exception: {} s", exceptionTime);
            return KeepAliveResponse.deadEntryPoint();
        }
    }

    public void applicationExitPoint(AuthorizeResponse authorizeResponse) {
        createRequest(authorizeResponse.toApplicationExitPoint()).get(String.class);
    }

    public AccountListResponse accountList(ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(applicationEntryPoint.toAccounts())
                .get(handelsbankenConfiguration.getAccountListResponse());
    }

    public HandelsbankenLoansResponse loans(ApplicationEntryPointResponse applicationEntryPoint) {
        return createRequest(applicationEntryPoint.toLoans())
                .get(handelsbankenConfiguration.getLoansResponse());
    }

    public AccountInfoResponse accountInfo(URL accountInfoUrl) {
        return createRequest(accountInfoUrl).get(AccountInfoResponse.class);
    }

    public abstract TransactionsResponse transactions(HandelsbankenAccount handelsbankenAccount);

    public abstract <CreditCard extends HandelsbankenCreditCard>
            CreditCardTransactionsResponse creditCardTransactions(CreditCard creditcard);

    public abstract <CreditCard extends HandelsbankenCreditCard>
            CreditCardTransactionsResponse<CreditCard> creditCardTransactions(URL url);

    protected RequestBuilder createPostRequest(URL url) {
        return createRequest(url).type(MediaType.APPLICATION_JSON);
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                // Have to set Accept header, otherwise Handelsbanken doesn't set a Content-Type or
                // returns error.
                .accept(
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_XML) // unexpectedly Handelsbanken may
                // return application/xml
                .header(
                        HandelsbankenConstants.Headers.X_SHB_DEVICE_NAME,
                        HandelsbankenConstants.Headers.DEVICE_NAME)
                .header(
                        HandelsbankenConstants.Headers.X_SHB_DEVICE_MODEL,
                        handelsbankenConfiguration.getDeviceModel())
                .header(
                        HandelsbankenConstants.Headers.X_SHB_DEVICE_CLASS,
                        HandelsbankenConstants.Headers.DEVICE_CLASS)
                .header(
                        HandelsbankenConstants.Headers.X_SHB_APP_VERSION,
                        handelsbankenConfiguration.getAppVersion());
    }
}
