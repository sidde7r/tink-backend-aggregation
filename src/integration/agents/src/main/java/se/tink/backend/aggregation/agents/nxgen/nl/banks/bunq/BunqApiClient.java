package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import com.google.common.base.Strings;
import java.security.PublicKey;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.CreateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.CreateSessionResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.InstallResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.InstallResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.InstallationRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.RegisterDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.RegisterDeviceResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional.rpc.AccountsResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional.rpc.TransactionsResponseWrapper;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class BunqApiClient {
    private final TinkHttpClient client;
    private final String userAgent;
    private final BunqConfiguration agentConfiguration;

    BunqApiClient(TinkHttpClient client, BunqConfiguration agentConfiguration) {
        this.client = client;
        this.userAgent = client.getUserAgent();
        this.agentConfiguration = agentConfiguration;
    }

    public void addFilter(Filter filter) {
        client.addFilter(filter);
    }

    public String getDefaultUserAgent() {
        return userAgent;
    }

    public InstallResponse installation(PublicKey publicKey) {
        InstallResponseWrapper response =
                client.request(getUrl(BunqConstants.Url.INSTALLATION))
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
                        ? BunqConstants.DEVICE_NAME
                        : aggregatorIdentifier;

        RegisterDeviceResponseWrapper response =
                client.request(getUrl(BunqConstants.Url.REGISTER_DEVICE))
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

    public CreateSessionResponse createSession(String apiKey) {
        CreateSessionResponseWrapper response =
                client.request(getUrl(BunqConstants.Url.CREATE_SESSION))
                        .post(
                                CreateSessionResponseWrapper.class,
                                CreateSessionRequest.createFromApiKey(apiKey));

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize CreateSessionResponse"));
    }

    public AccountsResponseWrapper listAccounts(String userId) {
        return client.request(
                        getUrl(BunqConstants.Url.MONETARY_ACCOUNTS)
                                .parameter(BunqConstants.UrlParameterKeys.USER_ID, userId))
                .get(AccountsResponseWrapper.class);
    }

    public TransactionsResponseWrapper listAccountTransactions(String userId, String accountId) {
        return client.request(
                        getUrl(BunqConstants.Url.MONETARY_ACCOUNTS_TRANSACTIONS)
                                .parameter(BunqConstants.UrlParameterKeys.USER_ID, userId)
                                .parameter(BunqConstants.UrlParameterKeys.ACCOUNT_ID, accountId))
                .queryParam(
                        BunqConstants.Pagination.TRANSACTIONS_PER_PAGE_KEY,
                        BunqConstants.Pagination.TRANSACTIONS_PER_PAGE_VALUE)
                .get(TransactionsResponseWrapper.class);
    }

    public TransactionsResponseWrapper listAccountTransactionsPagination(String nextPage) {
        return client.request(getUrl(nextPage)).get(TransactionsResponseWrapper.class);
    }

    private URL getUrl(String path) {
        return agentConfiguration.getUrl(path);
    }
}
