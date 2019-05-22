package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq;

import com.google.common.base.Strings;
import java.security.PublicKey;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.InstallResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.InstallResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.InstallationRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.RegisterDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.RegisterDeviceResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.fetchers.transactional.rpc.AccountsResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.fetchers.transactional.rpc.TransactionsResponseWrapper;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class BunqBaseApiClient {
    protected final TinkHttpClient client;
    protected final String userAgent;
    protected final String baseApiEndpoint;

    public BunqBaseApiClient(TinkHttpClient client, String baseApiEndpoint) {
        this.client = client;
        this.userAgent = client.getUserAgent();
        this.baseApiEndpoint = baseApiEndpoint;
    }

    public void addFilter(Filter filter) {
        client.addFilter(filter);
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
        String aggregatorName =
                Strings.isNullOrEmpty(aggregatorIdentifier)
                        ? BunqBaseConstants.DEVICE_NAME
                        : aggregatorIdentifier;

        RegisterDeviceResponseWrapper response =
                client.request(getUrl(BunqBaseConstants.Url.REGISTER_DEVICE))
                        .post(
                                RegisterDeviceResponseWrapper.class,
                                RegisterDeviceRequest.createFromApiKey(aggregatorName, apiKey));

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize RegisterDeviceResponse"));
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

    protected URL getUrl(String path) {
        return new URL(baseApiEndpoint + path);
    }
}
