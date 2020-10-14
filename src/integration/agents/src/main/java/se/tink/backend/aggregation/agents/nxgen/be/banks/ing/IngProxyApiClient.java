package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.AppAccessStatusResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.AuthenticateRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.AuthenticateResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.CreateEnrollmentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.CreateEnrollmentResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.FeatureTogglesResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.GetEnrollmentsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.GetSignResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.IndividualsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.SignRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.SignResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyAuthenticateRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyAuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyCreateEnrollmentRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyCreateEnrollmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyGetAppAccessStatusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyGetAppAccessStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyGetEnrollmentRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyGetEnrollmentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyGetFeatureTogglesRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyGetFeatureTogglesResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyGetSignRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyGetSignResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyIndividualsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyIndividualsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyOauthTokenRevokeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxyOauthTokenRevokeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxySignRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ProxySignResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.AgreementsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.TransactionsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.ProxyFilter;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyGetAgreementsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyGetAgreementsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyGetTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyGetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyResponseMessage;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class IngProxyApiClient {

    private static final int MAX_ATTEMPTS = 3;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TinkHttpClient tinkHttpClient;

    private final ProxyFilter proxyFilter;

    private final IngStorage ingStorage;

    public IngProxyApiClient(
            TinkHttpClient tinkHttpClient, ProxyFilter proxyFilter, IngStorage ingStorage) {
        this.tinkHttpClient = tinkHttpClient;
        this.proxyFilter = proxyFilter;
        this.ingStorage = ingStorage;
    }

    public AuthenticateResponseEntity authenticate(AuthenticateRequestEntity request) {
        ProxyAuthenticateResponse response =
                request()
                        .body(new ProxyAuthenticateRequest(request))
                        .post(ProxyAuthenticateResponse.class);
        validate(response, LoginError.INCORRECT_CREDENTIALS);
        return response.getContent();
    }

    public IndividualsResponseEntity getIndividuals() {
        ProxyIndividualsResponse response =
                request().body(new ProxyIndividualsRequest()).post(ProxyIndividualsResponse.class);
        validate(response, LoginError.DEFAULT_MESSAGE);
        return response.getContent();
    }

    public CreateEnrollmentResponseEntity enroll(CreateEnrollmentRequestEntity request) {
        ProxyCreateEnrollmentsResponse response =
                request()
                        .body(new ProxyCreateEnrollmentRequest(request))
                        .post(ProxyCreateEnrollmentsResponse.class);
        validate(response, LoginError.REGISTER_DEVICE_ERROR);
        return response.getContent();
    }

    public GetSignResponseEntity getSign(String id) {
        ProxyGetSignResponse response =
                request().body(new ProxyGetSignRequest(id)).post(ProxyGetSignResponse.class);
        validate(response, LoginError.DEFAULT_MESSAGE);
        return response.getContent();
    }

    public SignResponseEntity sign(SignRequestEntity request) {
        ProxySignResponse response =
                request().body(new ProxySignRequest(request)).post(ProxySignResponse.class);
        validate(response, LoginError.INCORRECT_CHALLENGE_RESPONSE);
        return response.getContent();
    }

    public GetEnrollmentsResponseEntity getEnrollment(String mobileAppId) {
        ProxyGetEnrollmentResponse response =
                request()
                        .body(new ProxyGetEnrollmentRequest(mobileAppId))
                        .post(ProxyGetEnrollmentResponse.class);
        validate(response, LoginError.DEFAULT_MESSAGE);
        return response.getContent();
    }

    public AppAccessStatusResponseEntity getAppAccessStatus() {
        // sometimes fail with 400 and no body in that case we retry
        ProxyGetAppAccessStatusResponse response;

        int retry = 0;
        while (retry++ < MAX_ATTEMPTS) {
            response =
                    request()
                            .body(new ProxyGetAppAccessStatusRequest())
                            .post(ProxyGetAppAccessStatusResponse.class);
            if (response.getStatus() < 300) {
                return response.getContent();
            }

            LOGGER.warn(
                    "Retrying app access status fetch due to wrong HTTP code attempt {}", retry);
        }

        throw BankServiceError.BANK_SIDE_FAILURE.exception();
    }

    public FeatureTogglesResponseEntity getFeatureTogglesStatus() {
        ProxyGetFeatureTogglesResponse response =
                request()
                        .body(new ProxyGetFeatureTogglesRequest())
                        .post(ProxyGetFeatureTogglesResponse.class);
        validate(response);
        return response.getContent();
    }

    public void revokeToken(String token) {
        ProxyOauthTokenRevokeResponse response =
                request()
                        .body(new ProxyOauthTokenRevokeRequest(token))
                        .post(ProxyOauthTokenRevokeResponse.class);
        validate(response);
    }

    public AgreementsResponseEntity getAgreements(String type) {
        ProxyGetAgreementsResponse response =
                request()
                        .body(new ProxyGetAgreementsRequest(type))
                        .post(ProxyGetAgreementsResponse.class);
        validate(response);
        return response.getContent();
    }

    public TransactionsResponseEntity getTransactionsFirstPage(String href, String accountType) {
        return getTransactions(href, "agreementType=" + accountType);
    }

    public TransactionsResponseEntity getTransactionsNextPages(String href, String query) {
        return getTransactions(href, query);
    }

    private TransactionsResponseEntity getTransactions(String href, String query) {
        ProxyGetTransactionsResponse response =
                request()
                        .body(new ProxyGetTransactionsRequest(href, query))
                        .post(ProxyGetTransactionsResponse.class);

        validate(response);
        return response.getContent();
    }

    private RequestBuilder request() {
        return tinkHttpClient
                .request(Urls.PROXY)
                .addFilter(proxyFilter)
                .header("Authorization", "Bearer " + ingStorage.getAccessToken());
    }

    private void validate(ProxyResponseMessage<?> response) {
        int status = response.getStatus();
        if (status >= 400) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }

    private void validate(ProxyResponseMessage<?> response, AgentError toThrowWhen400) {
        int status = response.getStatus();
        if (status >= 500) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        if (status >= 400) {
            throw toThrowWhen400.exception();
        }
    }
}
