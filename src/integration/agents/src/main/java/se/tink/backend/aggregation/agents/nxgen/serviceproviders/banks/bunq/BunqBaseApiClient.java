package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq;

import com.google.common.base.Strings;
import java.security.PublicKey;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.InstallResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.InstallResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.InstallationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.RegisterDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.RegisterDeviceResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.entities.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.rpc.AccountsResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.rpc.TransactionsResponseWrapper;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.cryptography.RSA;

public class BunqBaseApiClient {
    protected final TinkHttpClient client;
    protected final String userAgent;
    protected final String baseApiEndpoint;

    public BunqBaseApiClient(TinkHttpClient client, String baseApiEndpoint) {
        this.client = client;
        this.userAgent = client.getUserAgent();
        this.baseApiEndpoint = baseApiEndpoint;
    }

    public InstallResponse installation(PublicKey publicKey) {
        InstallResponseWrapper response =
                client.request(getUrl(BunqBaseConstants.Url.INSTALLATION))
                        .post(
                                InstallResponseWrapper.class,
                                InstallationRequest.createFromKey(
                                        RSA.pemFormatPublicKey(publicKey)));

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () -> new IllegalStateException("Could not deserialize InstallResponse"));
    }

    public RegisterDeviceResponse registerDevice(String apiKey, String aggregatorIdentifier) {
        try {
            return register(apiKey, aggregatorIdentifier);
        } catch (HttpResponseException e) {
            handleException(e);
            throw new IllegalStateException("Could not register device", e);
        }
    }

    public AccountsResponseWrapper listAccounts(String userId) {
        return client.request(
                        getUrl(BunqBaseConstants.Url.MONETARY_ACCOUNTS)
                                .parameter(BunqBaseConstants.UrlParameterKeys.USER_ID, userId))
                .get(AccountsResponseWrapper.class);
    }

    public TransactionsResponseWrapper listAccountTransactions(String userId, String accountId) {
        return client.request(
                        getUrl(BunqBaseConstants.Url.MONETARY_ACCOUNTS_TRANSACTIONS)
                                .parameter(BunqBaseConstants.UrlParameterKeys.USER_ID, userId)
                                .parameter(
                                        BunqBaseConstants.UrlParameterKeys.ACCOUNT_ID, accountId))
                .queryParam(
                        BunqBaseConstants.Pagination.TRANSACTIONS_PER_PAGE_KEY,
                        BunqBaseConstants.Pagination.TRANSACTIONS_PER_PAGE_VALUE)
                .get(TransactionsResponseWrapper.class);
    }

    public TransactionsResponseWrapper listAccountTransactionsPagination(String nextPage) {
        return client.request(getUrl(nextPage)).get(TransactionsResponseWrapper.class);
    }

    private RegisterDeviceResponse register(String apiKey, String aggregatorIdentifier) {
        String aggregatorName =
                Strings.isNullOrEmpty(aggregatorIdentifier)
                        ? BunqBaseConstants.DEVICE_NAME
                        : aggregatorIdentifier;

        RegisterDeviceResponseWrapper response =
                client.request(getUrl(BunqBaseConstants.Url.REGISTER_DEVICE))
                        .post(
                                RegisterDeviceResponseWrapper.class,
                                RegisterDeviceRequest.createFromApiKeyAllIPs(
                                        aggregatorName, apiKey));
        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize RegisterDeviceResponse"));
    }

    private static void handleException(HttpResponseException e) {
        String errorDescription =
                e.getResponse().getBody(ErrorResponse.class).getErrorDescription().get();
        if (errorDescription.equalsIgnoreCase(
                BunqBaseConstants.Errors.INCORRECT_USER_CREDENTIALS)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(e);
        } else if (errorDescription.equalsIgnoreCase(
                BunqBaseConstants.Errors.OPERATION_NOT_COMPLETED)) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(e);
        }
    }

    public URL getUrl(String path) {
        return new URL(baseApiEndpoint + path);
    }
}
